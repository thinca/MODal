package org.vim_jp.modal.mode;

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.scheduler.BukkitRunnable
import org.vim_jp.modal.MODalPlugin

class KikoriMode(plugin: MODalPlugin) extends Mode(plugin):
  override val MODE_NAME: String = "kikori"
  override val MODE_EXP_COST: Int = 7
  override val MODE_MATERIAL: Material = Material.GOLDEN_AXE
  override val MODE_CAPACITY: Int = 64

  def isLog(block: Block): Boolean =
    val name = block.getType.name
    name.endsWith("_LOG") ||
    name.endsWith("_STEM") ||
    name == "MANGROVE_ROOTS"

  def isLeave(block: Block): Boolean =
    val name = block.getType.name
    name.endsWith("_LEAVES")

  def tryBreak(player: Player, block: Block): Unit =
    new BukkitRunnable {
      override def run(): Unit =
        if !player.isValid || (!isLog(block) && !isLeave(block)) then return

        val y = -1
        val underBlocks = for
          x <- -1 to 1
          z <- -1 to 1
        yield block.getRelative(x, y, z)
        if underBlocks.forall(b => !isLog(b)) then
          block.breakNaturally()
          onLogBreak(player, block)
    }.runTaskLater(plugin, 1)

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
    if event.isCancelled then return

    val player = event.getPlayer
    if !isActive(player) then return

    val block = event.getBlock: Block
    if !isLog(block) then return

    consume(player)
    onLogBreak(player, block)
