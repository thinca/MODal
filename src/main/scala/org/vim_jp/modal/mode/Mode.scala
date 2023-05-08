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
import org.bukkit.persistence.PersistentDataType
import org.vim_jp.modal.MODalPlugin

import scala.jdk.CollectionConverters._

abstract class Mode(plugin: MODalPlugin) extends Listener:
  val MODE_NAME: String
  val MODE_EXP_COST: Int
  val MODE_MATERIAL: Material

  def isActive(player: Player): Boolean =
    val container = player.getPersistentDataContainer()
    val mode = container.get(plugin.modeDataKey, PersistentDataType.STRING)
    mode == MODE_NAME

  def activate(player: Player): Unit =
    val container = player.getPersistentDataContainer()
    container.set(plugin.modeDataKey, PersistentDataType.STRING, MODE_NAME)
    notifyActive(player)

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

  private def notifyActive(player: Player): Unit =
    player
      .spigot()
      .sendMessage(
        ChatMessageType.ACTION_BAR,
        TextComponent(s"mode changed: ${MODE_NAME}")
      )

    val bar = getOrNewBossBar(player)
    bar.setProgress(1)
    bar.setTitle(s"mode: ${MODE_NAME}")
    bar.addPlayer(player)
    bar.setVisible(true)

    getServer().broadcastMessage(
      s"${player.getDisplayName()} mode changed to ${MODE_NAME}"
    )

  def inactivate(player: Player): Unit =
    val container = player.getPersistentDataContainer()
    container.remove(plugin.modeDataKey)

    val bar = getBossBar(player)
    if bar != null then
      bar.removePlayer(player)
      bar.setVisible(false)

  // Notify the player if they having any mode
  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit =
    val player = event.getPlayer
    if isActive(player) then notifyActive(player)
