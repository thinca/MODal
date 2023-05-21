package org.vim_jp.modal.mode;

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.InventoryType.SlotType
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.vim_jp.modal.MODalPlugin

import scala.jdk.CollectionConverters._
import collection.mutable

object ModeChanging:
  private val modeForMaterial = mutable.Map[Material, Mode]()
  private val modeByName = mutable.Map[String, Mode]()

  def registerMode(mode: Mode): Unit =
    modeForMaterial(mode.MODE_MATERIAL) = mode
    modeByName(mode.MODE_NAME) = mode

class ModeChanging(plugin: MODalPlugin) extends Listener:
  private def createModeChanger(modeName: String): ItemStack =
    // Use Knowledge book as Mode Changer
    val book = ItemStack(Material.KNOWLEDGE_BOOK)
    val meta = book.getItemMeta
    meta.getPersistentDataContainer.set(
      plugin.modeNameDataKey,
      PersistentDataType.STRING,
      modeName
    )
    meta.setDisplayName(s"Mode changer: ${modeName}")
    book.setItemMeta(meta)
    return book

  private def getChangingMode(item: ItemStack): String =
    if item == null then return null
    if item.getType() != Material.KNOWLEDGE_BOOK then return null

    // if the book has no tag for the mode
    return item.getItemMeta.getPersistentDataContainer.get(
      plugin.modeNameDataKey,
      PersistentDataType.STRING
    )

  // Prepare a recipe to create Knowledge book
  @EventHandler
  def onPrepareAnvil(event: PrepareAnvilEvent): Unit =
    val inv: AnvilInventory = event.getInventory()

    val material = inv.getItem(inv.first(Material.BOOK) match
      case 0 => 1
      case 1 => 0
      case _ => return
    )
    if material == null then return

    val mode = ModeChanging.modeForMaterial(material.getType)

    if mode == null then return

    // Set result item (Knowledge book with the meta data)
    event.setResult(createModeChanger(mode.MODE_NAME))

    // Set EXP point cost
    new BukkitRunnable {
      override def run(): Unit =
        inv.setRepairCost(mode.MODE_EXP_COST)
    }.runTask(plugin)

  // Handle creation of the Knowledge book
  @EventHandler
  def onInventoryClick(event: InventoryClickEvent): Unit =
    // if the viewer is not a player, ignore it
    val players = event.getViewers()
    if players.size != 1 then return
    val player = players.get(0) match
      case player: Player => player
      case _              => return

    // if the cursor is not on the RESULT slot of the ANVIL, ignore it
    val anvil = event.getClickedInventory match
      case anvil: AnvilInventory => anvil
      case _                     => return
    if event.getSlotType() != InventoryType.SlotType.RESULT then return

    // if the item is not Mode Changer, ignore it
    val item = event.getCurrentItem
    val modeName = getChangingMode(item)
    if modeName == null then return

    // if the EXP cost is shortage, ignore it
    if anvil.getRepairCost > player.getLevel then return

    // consume materials
    val contents = anvil.getContents()
    anvil.setContents(
      contents.map(c =>
        c match
          case null => c
          case _ => {
            val next = c.clone
            next.setAmount(next.getAmount() - 1)
            next
          }
      )
    )

    new BukkitRunnable {
      override def run(): Unit =
        // give the item to the viewer
        player.setItemOnCursor(item)
        // consume EXP cost
        player.setLevel(player.getLevel - anvil.getRepairCost)
    }.runTask(plugin)

  // Handle usage of the Mode Changer
  @EventHandler
  def onPlayerInteract(event: PlayerInteractEvent): Unit =
    val actionType = event.getAction match
      case  Action.RIGHT_CLICK_AIR =>
        "AIR"
      case  Action.RIGHT_CLICK_BLOCK => {
        if event.getClickedBlock.getType.isInteractable then null
        else "BLOCK"
      }
      case _ => null

    if actionType == null then return

    val modeName = getChangingMode(event.getItem)
    if modeName == null then return

    ModeChanging.modeByName(modeName).activate(event.getPlayer)
