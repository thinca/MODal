package org.vim_jp.modal

import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.vim_jp.modal.mode.FarmerMode
import org.vim_jp.modal.mode.KikoriMode
import org.vim_jp.modal.mode.ModeChanging
import org.vim_jp.modal.mode.TeleporterMode

import java.io.BufferedReader
import java.io.InputStreamReader

object MODalPlugin:
  val MODE_NAME_DATA_KEY: String = "mode-name"
  val MODE_CAPACITY_DATA_KEY: String = "mode-capacity"

class MODalPlugin extends JavaPlugin:
  outer =>

  val modeNameDataKey = NamespacedKey(outer, MODalPlugin.MODE_NAME_DATA_KEY)
  val modeCapacityDataKey =
    NamespacedKey(outer, MODalPlugin.MODE_CAPACITY_DATA_KEY)
  val modes = Set(FarmerMode(this), KikoriMode(this), TeleporterMode(this))

  override def onEnable(): Unit =
    val server = getServer

    val pluginManager = server.getPluginManager
    pluginManager.registerEvents(ModeChanging(this), this)
    modes.foreach(m => {
      ModeChanging.registerMode(m)
      pluginManager.registerEvents(m, this)
    })

    this.getCommand("change").setExecutor(ChangeCommand())
    this.getCommand("inactivate").setExecutor(InactivateCommand())

    val stream = outer.getResource("commit_hash")
    if stream != null then
      val br = BufferedReader(InputStreamReader(stream))
      val hash = br.readLine
      server.broadcastMessage(
        s"${ChatColor.GREEN}MODal enabled: ${ChatColor.YELLOW}${hash}"
      )
    else server.broadcastMessage(s"${ChatColor.GREEN}MODal enabled")

  // A definition for the command to target caller-own.
  abstract class PlayerCommand extends CommandExecutor:

    def action(player: Player, args: Array[String]): Boolean

    override def onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array[String]
    ): Boolean =
      val player = sender match
        case player: Player => player
        case _              => return false
      action(player, args)

  class ChangeCommand extends PlayerCommand:
    def action(player: Player, args: Array[String]): Boolean =
      if args.length != 1 then return false
      val mode = args(0)
      modes.filter(m => m.MODE_NAME == mode).foreach(m => m.activate(player))
      true

  class InactivateCommand extends PlayerCommand:
    def action(player: Player, args: Array[String]): Boolean =
      modes.filter(m => m.isActive(player)).foreach(m => m.inactivate(player))
      true
