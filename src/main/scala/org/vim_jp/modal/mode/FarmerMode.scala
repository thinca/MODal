package org.vim_jp.modal.mode;

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.vim_jp.modal.MODalPlugin

class FarmerMode(plugin: MODalPlugin) extends Mode(plugin):
  override val MODE_NAME: String = "farmer"
  override val MODE_EXP_COST: Int = 5
  override val MODE_MATERIAL: Material = Material.GOLDEN_HOE
  override val MODE_CAPACITY: Int = 128

  def seedOf(block: Block): Material =
    block.getType.name match
      case "WHEAT"    => Material.WHEAT_SEEDS
      case "POTATOES" => Material.POTATO
      case _          => null

  @EventHandler
  def onBlockBreak(event: BlockBreakEvent): Unit =
    if event.isCancelled then return

    val player = event.getPlayer

    if !isActive(player) then return

    val block = event.getBlock: Block

    val seed = seedOf(block)
    if seed == null then return

    val ageable = block.getBlockData match
      case ageable: Ageable => ageable
      case _                => return
    if ageable.getAge != ageable.getMaximumAge then
      event.setCancelled(true)
      return

    // consume seed
    val item = ItemStack(seed)
    val removed = player.getInventory().removeItem(item)
    // (there's no seed)
    if !removed.isEmpty then return

    // consume capacity
    consume(player)

    // set block to younuest state
    val typ = block.getType
    val loc = block.getLocation()
    new BukkitRunnable {
      override def run(): Unit =
        val newBlock = loc.getBlock
        newBlock.setType(typ)
    }.runTask(plugin)
