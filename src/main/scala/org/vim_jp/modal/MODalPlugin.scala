package org.vim_jp.modal

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.{Listener, EventHandler}
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.entity.Player
import org.bukkit.Server
import org.bukkit.scheduler.BukkitRunnable

class MODalPlugin extends JavaPlugin:
  outer =>

  override def onEnable(): Unit =
    val server = getServer
    val pluginManager = server.getPluginManager
    pluginManager.registerEvents(Kikori(), this)

  class Kikori extends Listener:
    def isLog(block: Block): Boolean =
      block.getType.name.endsWith("_LOG")

    def tryBreak(player: Player, block: Block): Unit =
      new BukkitRunnable:
        override def run(): Unit =
          if !player.isValid || !isLog(block) then return

          val y = -1
          val underBlocks = for
            x <- -1 to 1
            z <- -1 to 1
          yield
            block.getRelative(x, y, z)
          if underBlocks.forall(!_.getType.isOccluding) then
            block.breakNaturally()
            onLogBreak(player, block)
      .runTaskLater(outer, 1)

    def onLogBreak(player: Player, block: Block): Unit =
      for
        y <- 0 to 1
        x <- -1 to 1
        z <- -1 to 1
        if x != 0 || y != 0 || z != 0
      do
        val nextBlock = block.getRelative(x, y, z)
        tryBreak(player, nextBlock)

    @EventHandler
    def onBlockBreak(event: BlockBreakEvent): Unit =
      val player = event.getPlayer
      val block = event.getBlock: Block

      if !isLog(block) then return

      val itemInHand = player.getInventory.getItemInMainHand
      if itemInHand.getType == Material.GOLDEN_AXE then
        onLogBreak(player, block)
