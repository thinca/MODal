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
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.InventoryType.SlotType
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerJoinEvent
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

  private def createKnowledgeBook(): ItemStack =
    val book = ItemStack(Material.KNOWLEDGE_BOOK)
    val meta = book.getItemMeta
    meta.getPersistentDataContainer.set(
      plugin.modeDataKey,
      PersistentDataType.STRING,
      MODE_NAME
    )
    meta.setLore(List(s"mode: ${MODE_NAME}").asJava)
    book.setItemMeta(meta)
    return book

  // Prepare a recipe to create Knowledge book to change mode
  @EventHandler
  def onPrepareAnvil(event: PrepareAnvilEvent): Unit =
    val inventory = event.getInventory()

    if !inventory.contains(MODE_MATERIAL) then return
    if !inventory.contains(Material.BOOK) then return

    // Set result item (Knowledge book with the meta data)
    event.setResult(createKnowledgeBook())

    // Set EXP point cost
    new BukkitRunnable {
      override def run(): Unit =
        inventory.setRepairCost(MODE_EXP_COST)
    }.runTask(plugin)

  // Handle creation of the Knowledge book to change mode
  @EventHandler
  def onInventoryClick(event: InventoryClickEvent): Unit =
    // if the viewer is not a player, ignore it
    val players = event.getViewers()
    if players.size != 1 then return
    val player = players.get(0) match
      case player: Player => player
      case _              => return

    // if the cursor is not on the RESULT slot of the ANVIL, ignore it
    val inventory = event.getClickedInventory()
    if inventory.getType() != InventoryType.ANVIL then return
    if event.getSlotType() != InventoryType.SlotType.RESULT then return

    // if the current item is not Knowledge book, ignore it
    val current = event.getCurrentItem()
    if current == null then return
    if current.getType() != Material.KNOWLEDGE_BOOK then return

    // if the book has no tag for the mode, ignore it
    val metadata = current.getItemMeta.getPersistentDataContainer.get(
      plugin.modeDataKey,
      PersistentDataType.STRING
    )
    if metadata != MODE_NAME then return

    // consume materials
    val contents = inventory.getContents()
    inventory.setContents(
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

    // give the book to the viewer
    player.setItemOnCursor(current)
