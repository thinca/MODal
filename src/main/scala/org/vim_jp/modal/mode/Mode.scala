package org.vim_jp.modal.mode;

import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataType
import org.vim_jp.modal.MODalPlugin

abstract class Mode(plugin: MODalPlugin) extends Listener:
  val MODE_NAME: String

  def isActive(player: Player): Boolean =
    val container = player.getPersistentDataContainer()
    val mode = container.get(plugin.modeDataKey, PersistentDataType.STRING)
    mode == MODE_NAME
