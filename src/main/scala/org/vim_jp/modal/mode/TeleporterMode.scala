package org.vim_jp.modal.mode;

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.entity.SpectralArrow
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.ProjectileHitEvent
import org.vim_jp.modal.MODalPlugin

class TeleporterMode(plugin: MODalPlugin) extends Mode(plugin):
  override val MODE_NAME: String = "teleporter"
  override val MODE_EXP_COST: Int = 5
  override val MODE_MATERIAL: Material = Material.ENDER_PEARL
  override val MODE_CAPACITY: Int = 64

  def isSafeBlock(block: Block): Boolean =
    val blockType = block.getType
    block.isPassable && blockType != Material.WATER && blockType != Material.LAVA

  @EventHandler
  def onProjectileHit(event: ProjectileHitEvent): Unit =
    if event.isCancelled then return

    val projectile = event.getEntity

    val arrow = projectile match
      case arrow: SpectralArrow => arrow
      case _                    => return

    val player = arrow.getShooter match
      case p: Player => p
      case _         => return

    if !isActive(player) then return

    val hitBlock = event.getHitBlock
    if hitBlock == null then return

    val warpPos = hitBlock.getRelative(event.getHitBlockFace)

    if !warpPos.getRelative(BlockFace.DOWN).getType.isSolid ||
      !isSafeBlock(warpPos) ||
      !isSafeBlock(warpPos.getRelative(BlockFace.UP))
    then return

    consume(player)

    arrow.remove()
    player.teleport(
      warpPos.getLocation
        .add(0.5, 0, 0.5)
        .setDirection(player.getLocation.getDirection)
    )
