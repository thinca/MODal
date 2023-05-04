package org.vim_jp.modal
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.entity.SpectralArrow
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.vim_jp.modal.mode.FarmerMode

object MODalPlugin:
  val MODE_DATA_KEY: String = "mode"

class MODalPlugin extends JavaPlugin:
  outer =>

  val modeDataKey = NamespacedKey(outer, MODalPlugin.MODE_DATA_KEY)
  val modes = Set(FarmerMode(this))

  override def onEnable(): Unit =
    val server = getServer
    val pluginManager = server.getPluginManager
    pluginManager.registerEvents(Kikori(), this)
    pluginManager.registerEvents(ArrowWarp(), this)
    modes.foreach(m => pluginManager.registerEvents(m, this))

    this.getCommand("change").setExecutor(CommandModal())

  class CommandModal extends CommandExecutor:
    override def onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array[String]
    ): Boolean =
      val player = sender match
        case player: Player => player
        case _              => return false

      if args.length != 1 then return false

      this.setMode(player, args(0))

      // TODO: use args to decide target
      // like `/modal:change @s farmer`
      true

    def setMode(player: Player, mode: String): Unit =
      modes.filter(m => m.MODE_NAME == mode).foreach(m => m.activate(player))

  // TODO: create TabCompleteEvent for the command

  class Kikori extends Listener:
    def isLog(block: Block): Boolean =
      val name = block.getType.name
      name.endsWith("_LOG") ||
      name.endsWith("_STEM") ||
      name == "MANGROVE_ROOTS"

    def tryBreak(player: Player, block: Block): Unit =
      new BukkitRunnable {
        override def run(): Unit =
          if !player.isValid || !isLog(block) then return

          val y = -1
          val underBlocks = for
            x <- -1 to 1
            z <- -1 to 1
          yield block.getRelative(x, y, z)
          if underBlocks.forall(!_.getType.isOccluding) then
            block.breakNaturally()
            onLogBreak(player, block)
      }.runTaskLater(outer, 1)

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

  class ArrowWarp extends Listener:
    def isSafeBlock(block: Block): Boolean =
      val blockType = block.getType
      block.isPassable && blockType != Material.WATER && blockType != Material.LAVA

    @EventHandler
    def onProjectileHit(event: ProjectileHitEvent): Unit =
      val projectile = event.getEntity

      val arrow = projectile match
        case arrow: SpectralArrow => arrow
        case _                    => return

      val player = arrow.getShooter match
        case p: Player => p
        case _         => return

      val hitBlock = event.getHitBlock
      if hitBlock == null then return

      val warpPos = hitBlock.getRelative(event.getHitBlockFace)

      if warpPos.getRelative(BlockFace.DOWN).getType.isSolid &&
        isSafeBlock(warpPos) && isSafeBlock(warpPos.getRelative(BlockFace.UP))
      then
        arrow.remove()
        player.teleport(
          warpPos.getLocation
            .add(0.5, 0, 0.5)
            .setDirection(player.getLocation.getDirection)
        )
