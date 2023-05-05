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
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.InventoryType.SlotType
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
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
    notifyInActive(player)

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

  private def notifyInActive(player: Player): Unit =
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
    if isActive(player) then notifyInActive(player)

  private def createModeChanger(): ItemStack =
    // Use Knowledge book as Mode Changer
    val book = ItemStack(Material.KNOWLEDGE_BOOK)
    val meta = book.getItemMeta
    meta.getPersistentDataContainer.set(
      plugin.modeDataKey,
      PersistentDataType.STRING,
      MODE_NAME
    )
    meta.setDisplayName(s"Mode changer: ${MODE_NAME}")
    book.setItemMeta(meta)
    return book

  private def isModeChanger(item: ItemStack): Boolean =
    if item == null then return false
    if item.getType() != Material.KNOWLEDGE_BOOK then return false

    // if the book has no tag for the mode
    MODE_NAME == item.getItemMeta.getPersistentDataContainer.get(
      plugin.modeDataKey,
      PersistentDataType.STRING
    )

  // Prepare a recipe to create Knowledge book
  @EventHandler
  def onPrepareAnvil(event: PrepareAnvilEvent): Unit =
    val inventory: AnvilInventory = event.getInventory()

    if !inventory.contains(MODE_MATERIAL) then return
    if !inventory.contains(Material.BOOK) then return

    // Set result item (Knowledge book with the meta data)
    event.setResult(createModeChanger())

    // Set EXP point cost
    new BukkitRunnable {
      override def run(): Unit =
        inventory.setRepairCost(MODE_EXP_COST)
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
    if !isModeChanger(item) then return

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
    val action = event.getAction
    if action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK
    then return

    if !isModeChanger(event.getItem) then return

    activate(event.getPlayer)
