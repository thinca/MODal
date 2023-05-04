package org.vim_jp.modal.mode;

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
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

abstract class Mode(plugin: MODalPlugin) extends Listener:
  val MODE_NAME: String

  def isActive(player: Player): Boolean =
    val container = player.getPersistentDataContainer()
    val mode = container.get(plugin.modeDataKey, PersistentDataType.STRING)
    mode == MODE_NAME

  def activate(player: Player): Unit =
    val container = player.getPersistentDataContainer()
    container.set(plugin.modeDataKey, PersistentDataType.STRING, MODE_NAME)
    notifyInActive(player)

  def getBossBarKey(player: Player): NamespacedKey =
    return NamespacedKey(
      plugin,
      s"mode-bossbar-${player.getUniqueId}"
    )

  def getBossBar(player: Player): BossBar =
    val key = getBossBarKey(player)
    return Bukkit.getBossBar(key)

  def getOrNewBossBar(player: Player): BossBar =
    val key = getBossBarKey(player)
    val bar = Bukkit.getBossBar(key)
    return bar match
      case null =>
        Bukkit.createBossBar(
          getBossBarKey(player),
          s"mode: ${MODE_NAME}",
          BarColor.YELLOW,
          BarStyle.SOLID
        )
      case _ => bar

  def notifyInActive(player: Player): Unit =
    player
      .spigot()
      .sendMessage(
        ChatMessageType.ACTION_BAR,
        TextComponent(s"mode changed: ${MODE_NAME}")
      )

    val bar = getOrNewBossBar(player)
    bar.setProgress(1)
    bar.addPlayer(player)
    bar.show()

  def inactivate(player: Player): Unit =
    val container = player.getPersistentDataContainer()
    container.remove(plugin.modeDataKey)

    val bar = getBossBar(player)
    if bar != null then bar.removePlayer(player)

  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit =
    val player = event.getPlayer
    if isActive(player) then notifyInActive(player)
