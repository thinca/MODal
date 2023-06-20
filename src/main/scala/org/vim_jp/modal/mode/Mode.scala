package org.vim_jp.modal.mode;

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.persistence.PersistentDataType
import org.vim_jp.modal.MODalPlugin

import scala.jdk.CollectionConverters._

abstract class Mode(plugin: MODalPlugin) extends Listener:
  val MODE_NAME: String
  val MODE_EXP_COST: Int
  val MODE_MATERIAL: Material
  val MODE_CAPACITY: Int

  def isActive(player: Player): Boolean =
    val container = player.getPersistentDataContainer()
    val mode = container.get(plugin.modeNameDataKey, PersistentDataType.STRING)
    mode == MODE_NAME

  def activate(player: Player): Unit =
    val container = player.getPersistentDataContainer()
    container.set(plugin.modeNameDataKey, PersistentDataType.STRING, MODE_NAME)
    container.set(
      plugin.modeCapacityDataKey,
      PersistentDataType.INTEGER,
      MODE_CAPACITY
    )
    notifyActive(player)
    updateCapacityView(player, Some(MODE_CAPACITY))

  private def getBossBarKey(player: Player): NamespacedKey =
    return NamespacedKey(
      plugin,
      s"mode-bossbar-${player.getUniqueId}"
    )

  private def getBossBar(player: Player): BossBar =
    val key = getBossBarKey(player)
    return Bukkit.getBossBar(key)

  private def getOrNewBossBar(player: Player): BossBar =
    val key = getBossBarKey(player)
    val bar = Bukkit.getBossBar(key)
    return bar match
      case null =>
        Bukkit.createBossBar(
          getBossBarKey(player),
          "",
          BarColor.YELLOW,
          BarStyle.SOLID
        )
      case _ => bar

  private def updateCapacityView(
      player: Player,
      current: Option[Integer] = None
  ): Unit =
    val cur = current match
      case None =>
        player.getPersistentDataContainer.get(
          plugin.modeCapacityDataKey,
          PersistentDataType.INTEGER
        )
      case Some(v) => v
    if cur == 0 || cur == null then
      val bar = getBossBar(player)
      if bar != null then
        bar.removePlayer(player)
        bar.setVisible(false)
    else
      val bar = getOrNewBossBar(player)
      bar.addPlayer(player)
      bar.setProgress(cur.toDouble / MODE_CAPACITY)
      bar.setTitle(s"mode: ${MODE_NAME}")
      bar.setVisible(true)

  private def notifyActive(player: Player): Unit =
    player
      .spigot()
      .sendMessage(
        ChatMessageType.ACTION_BAR,
        TextComponent(s"mode changed: ${MODE_NAME}")
      )

    plugin.getServer.broadcastMessage(
      s"${player.getDisplayName()} mode changed to ${MODE_NAME}"
    )

  def inactivate(player: Player): Unit =
    val container = player.getPersistentDataContainer()
    container.remove(plugin.modeNameDataKey)
    container.remove(plugin.modeCapacityDataKey)
    updateCapacityView(player)

  // Notify the player if they having any mode
  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit =
    val player = event.getPlayer
    if isActive(player) then updateCapacityView(player)

  @EventHandler
  def onPlayerRespawnEvent(event: PlayerRespawnEvent): Unit =
    val player = event.getPlayer
    inactivate(player)

  def consume(player: Player): Unit =
    val container = player.getPersistentDataContainer()
    val next = container.get(
      plugin.modeCapacityDataKey,
      PersistentDataType.INTEGER
    ) - 1
    if next < 0 then inactivate(player)
    else
      container.set(
        plugin.modeCapacityDataKey,
        PersistentDataType.INTEGER,
        next
      )
      updateCapacityView(player, Some(next))
