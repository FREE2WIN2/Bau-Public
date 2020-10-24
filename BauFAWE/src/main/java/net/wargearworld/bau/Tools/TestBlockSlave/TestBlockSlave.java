package net.wargearworld.bau.tools.testBlockSlave;

import java.util.*;
import java.util.Map.Entry;

import net.wargearworld.bau.player.BauPlayer;
import net.wargearworld.bau.utils.CoordGetter;
import net.wargearworld.bau.world.plots.Plot;
import net.wargearworld.db.model.TestBlock;
import net.wargearworld.db.model.TestBlock_;
import net.wargearworld.db.model.enums.schematic.SchematicDirection;
import net.wargearworld.thedependencyplugin.DependencyProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.CharMatcher;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.session.ClipboardHolder;

import net.wargearworld.bau.Main;
import net.wargearworld.bau.MessageHandler;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.CustomTestBlock;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.EmptyTestBlock;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.Facing;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.ITestBlock;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.TestBlockType;
import net.wargearworld.bau.tools.testBlockSlave.testBlock.Type;
import net.wargearworld.bau.worldedit.UndoManager;
import net.wargearworld.bau.worldedit.WorldEditHandler;
import net.wargearworld.bau.worldedit.WorldGuardHandler;
import net.wargearworld.bau.utils.ClickAction;
import net.wargearworld.bau.utils.JsonCreater;
import net.wargearworld.bau.utils.Scheduler;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class TestBlockSlave {

	/*
	 * this is the personal testBlockSlave for every Player it handles all personal
	 * testblocks and you can open the editor over him and save own testblocks.
	 * 
	 */
	private UUID owner;
	private HashMap<Integer, Set<CustomTestBlock>> testblocks;
	private ITestBlock last;
	private Facing lastFacing;
	private UndoManager undoManager;
	private Scheduler saveTBParticles;
	private EmptyTestBlock newTBToSave;
	private ChooseTestBlock chooseTB;
	/* init */

	public TestBlockSlave(UUID owner) {
		this.owner = owner;
		testblocks = new HashMap<>();
		testblocks.put(1, readTestBlocks(1));
		testblocks.put(2, readTestBlocks(2));
		testblocks.put(3, readTestBlocks(3));
		last = null;
		undoManager = new UndoManager(owner);
		saveTBParticles = new Scheduler();
	}

	private Set<CustomTestBlock> readTestBlocks(int tier) {
		HashSet<CustomTestBlock> outSet = new HashSet<>();
		EntityManager em = DependencyProvider.getEntityManager();
		net.wargearworld.db.model.Player dbPlayer = (net.wargearworld.db.model.Player) em.find(net.wargearworld.db.model.Player.class,owner);

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery cq = cb.createQuery(net.wargearworld.db.model.TestBlock.class);
		Root root = cq.from(net.wargearworld.db.model.TestBlock.class);
		cq.where(cb.equal(root.get(TestBlock_.OWNER),dbPlayer),cb.equal(root.get(TestBlock_.TIER),tier));

		Query query = em.createQuery(cq);
		List<net.wargearworld.db.model.TestBlock> testBlocks = query.getResultList();
		for(net.wargearworld.db.model.TestBlock block:testBlocks){
			outSet.add(CustomTestBlock.fromDb(block));
		}
		return outSet;
	}

	private HashSet<CustomTestBlock> readFavs() {
		HashSet<CustomTestBlock> favs = new HashSet<>();
		for (Entry<Integer, Set<CustomTestBlock>> tbs : testblocks.entrySet()) {
			for (CustomTestBlock block : tbs.getValue()) {
				if (block.isFavorite()) {
					favs.add(block);
				}
			}
		}
		return favs;
	}

	public void openGUI() {
		Player ownerPlayer = Bukkit.getPlayer(owner);
		ownerPlayer.openInventory(TestBlockSlaveGUI.tbsStartInv(ownerPlayer, readFavs()));
	}

	/* Getter */

	public ITestBlock getlastTestBlock() {
		return last;
	}

	public Facing getLastFacing() {
		return lastFacing;
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}

	public ChooseTestBlock getChooseTB() {
		return chooseTB;
	}

	/* CHOOSING */

	public void startNewChoose() {
		chooseTB = new ChooseTestBlock();
	}

	public void startNewChoose(TestBlockType tbtype, int i) {// Defaults
		chooseTB = new ChooseTestBlock();
		chooseTB.setTier(i);
		chooseTB.setTestBlockType(tbtype);
	}

	public void startNewChoose(ItemStack clicked) {// Customs
		chooseTB = new ChooseTestBlock();
		chooseTB.setTestBlock(getBlockOutOfBanner(clicked));
		chooseTB.setTestBlockType(TestBlockType.CUSTOM);
	}

	/* paste a testBlock */

	public void pasteBlock(ITestBlock block, Facing facing, boolean saveUndo) {
		Player ownerPlayer = Bukkit.getPlayer(owner);
		Plot plot = Objects.requireNonNull(BauPlayer.getBauPlayer(owner).getCurrentPlot());
		/* paste */
		MessageHandler.getInstance().send(ownerPlayer,"tbs_paste_undoMessage");
		WorldEditHandler.pasteTestBlock(block.getSchematic(), facing, plot, ownerPlayer, saveUndo);
		last = block;
		lastFacing = facing;

	}

	public void pasteBlock(ChooseTestBlock chooseTB2, boolean saveUndo) {
		pasteBlock(chooseTB2.getTestBlock(), chooseTB2.getFacing(), saveUndo);
	}

	public void undo() {
		/* Undo last TB */
		BauPlayer bauPlayer = BauPlayer.getBauPlayer(owner);
		Clipboard undo = undoManager.getUndo();
		if (undo == null) {
			MessageHandler.getInstance().send(bauPlayer, "tbs_noUndo");
			return;
		}
		WorldEditHandler.pasteAsync(new ClipboardHolder(undo), undo.getOrigin(), bauPlayer.getBukkitPlayer(), false, 1, false, true);
		MessageHandler.getInstance().send(bauPlayer, "tbs_undo");
	}

	/* Adding TestBlocks */

	public boolean addNewCustomTestBlock(String name) {
		int tier = newTBToSave.getTier();
		Plot plot = newTBToSave.getPlot();
		Facing facing = newTBToSave.getfacing();
		Type type = newTBToSave.getType();
		if (testblocks.get(tier).size() == 9) {
			MessageHandler.getInstance().send(owner, "tbs_tooManyBlocks", "" + tier);
			return false;
		}
		if (nameExists(tier, name)) {
			MessageHandler.getInstance().send(owner, "tbs_nameNotFree", "" + tier, name);
			return false;
		}
		saveRegionAsBlock(tier, facing, plot, name,type);
		Set<CustomTestBlock> adding = testblocks.get(tier);
		CustomTestBlock block = new CustomTestBlock(owner, name, facing, tier);
		adding.add(block);
		testblocks.put(tier, adding);
		putTestBlockToDatabase(block);
		return true;
	}

	private void putTestBlockToDatabase(CustomTestBlock block) {
		EntityManager em = DependencyProvider.getEntityManager();
		em.getTransaction().begin();

		net.wargearworld.db.model.TestBlock tb = new net.wargearworld.db.model.TestBlock();
		tb.setName(block.getName());
		tb.setTier(block.getTier());
		tb.setFavorite(block.isFavorite());
		tb.setDirection(SchematicDirection.valueOf(block.getSchematic().getFacing().name()));
		tb.setOwner(em.find(net.wargearworld.db.model.Player.class,block.getOwner()));

		em.persist(tb);
		em.getTransaction().commit();
		em.close();
	}

	private void saveRegionAsBlock(int tier, Facing facing, Plot plot, String name, Type type) {
		Player ownerPlayer = Bukkit.getPlayer(owner);
		if(ownerPlayer == null) return;

		Region rg = TestBlockSlaveCore.getTBRegion(tier, plot, facing);
		if(type.equals(Type.SHIELDS)) {
			int shielsSize = TestBlockSlaveCore.getMaxShieldSizeOfTier(tier);
			try {
				rg.expand(BlockVector3.at(shielsSize, shielsSize, shielsSize),BlockVector3.at(-shielsSize, 0, -shielsSize));
			} catch (RegionOperationException e) {
			}
		}
		Clipboard board = WorldEditHandler.createClipboardOutOfRegion(rg,
				CoordGetter.getTBSPastePosition(plot, facing), BukkitAdapter.adapt(ownerPlayer.getWorld()));
		WorldEditHandler.saveClipboardAsSchematic(
				Main.schempath + "/" + owner.toString() + "/TestBlockSklave", name + ".schem", board);

	}

	public boolean setTestBlockToFavorite(ITestBlock tb) {
		if (readFavs().size() == 9) {
			MessageHandler.getInstance().send(owner, "tbs_tooManyFavorites", "fa");
		}
		if (tb instanceof CustomTestBlock) {
			CustomTestBlock block = (CustomTestBlock) tb;
			block.setFavorite(true);
			updateFavToDataBase(true, block.getTier(), block.getName());
			MessageHandler.getInstance().send(owner, "tbs_favAdded", block.getName());
			return true;
		}
		return false;
	}

	public void setTestBlockToFavorite(ItemStack clicked) {
		setTestBlockToFavorite(getBlockOutOfBanner(clicked));

	}

	/* Removing TestBlocks */

	public boolean removeFavorite(ITestBlock tb) {
		if (tb instanceof CustomTestBlock) {
			CustomTestBlock block = (CustomTestBlock) tb;
			if (!block.isFavorite()) {
				MessageHandler.getInstance().send(owner, "tbs_blockIsNoFavorite");
				return false;
			}
			block.setFavorite(false);
			updateFavToDataBase(false, block.getTier(), block.getName());
			MessageHandler.getInstance().send(owner, "tbs_favRemoved", block.getName());
			return true;
		}
		return false;

	}

	public boolean deleteTestBlock(int tier, String name) {
		Set<CustomTestBlock> blocks = testblocks.get(tier);
		if (!deleteTestBlockFromDatabase(tier, name)) {
			return false;
		}
		ITestBlock tb = getBlockOutOfName(name, tier);
		if (!blocks.remove(tb)) {
			return false;
		}
		tb.getSchematic().getFile().delete();
		MessageHandler.getInstance().send(owner, "tbs_tbDeleted", "" + tier, name);
		return true;
	}

	private boolean deleteTestBlockFromDatabase(int tier, String name) {
		EntityManager em = DependencyProvider.getEntityManager();
		em.getTransaction().begin();
		net.wargearworld.db.model.Player dbPlayer = em.find(net.wargearworld.db.model.Player.class,owner);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<TestBlock> cq = cb.createQuery(TestBlock.class);
		Root<TestBlock> root = cq.from(TestBlock.class);

		cq.where(cb.equal(root.get(TestBlock_.OWNER),dbPlayer), cb.equal(root.get(TestBlock_.NAME),name), cb.equal(root.get(TestBlock_.TIER),tier));
		Query query = em.createQuery(cq);
		TestBlock tb = (TestBlock) query.getSingleResult();
		em.remove(tb);
		em.getTransaction().commit();
		em.close();
		return true;
	}

	/* Helper Methods */

	public CustomTestBlock getBlockOutOfBanner(ItemStack itemStack) {
		for (Entry<Integer, Set<CustomTestBlock>> testBlockEntries : testblocks.entrySet()) {
			for (CustomTestBlock block : testBlockEntries.getValue()) {
				if (block.getBanner().equals(itemStack)) {
					return block;
				}
			}
		}
		return null;
	}

	private ITestBlock getBlockOutOfName(String name, int tier) {
		for (CustomTestBlock block : testblocks.get(tier)) {
			if (block.getName().equals(name)) {
				return block;
			}
		}
		return null;
	}

	private boolean nameExists(int tier, String name) {
		for (CustomTestBlock block : testblocks.get(tier)) {
			if (block.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	private boolean updateFavToDataBase(boolean fav, int tier, String name) {
		EntityManager em = DependencyProvider.getEntityManager();
		em.getTransaction().begin();
		net.wargearworld.db.model.Player dbPlayer = em.find(net.wargearworld.db.model.Player.class,owner);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<TestBlock> cq = cb.createQuery(TestBlock.class);
		Root<TestBlock> root = cq.from(TestBlock.class);

		cq.where(cb.equal(root.get(TestBlock_.OWNER),dbPlayer), cb.equal(root.get(TestBlock_.NAME),name), cb.equal(root.get(TestBlock_.TIER),tier));
		Query query = em.createQuery(cq);
		TestBlock tb = (TestBlock) query.getSingleResult();
		tb.setFavorite(fav);
		em.merge(tb);
		em.getTransaction().commit();
		em.close();
		return true;
	}
	/* Show ManageMent Inventory */

	public void showTBManager() {
		Player ownerPlayer  = Bukkit.getPlayer(owner);
		if(ownerPlayer == null) return;
		ownerPlayer.openInventory(TestBlockSlaveGUI.tbManager(testblocks, ownerPlayer));
	}

	public void openAddFavoriteInv() {
		Player ownerPlayer  = Bukkit.getPlayer(owner);
		if(ownerPlayer == null) return;
		ownerPlayer.openInventory(TestBlockSlaveGUI.showAllNonFavorites(testblocks, ownerPlayer));
	}

	public void startSavingNewTB() {
		Player ownerPlayer  = Bukkit.getPlayer(owner);
		if(ownerPlayer == null) return;
		startNewChoose();
		chooseTB.setTestBlockType(TestBlockType.NEW);
		ownerPlayer.openInventory(TestBlockSlaveGUI.tierInv(ownerPlayer));

	}

	public void savingNewTBName() {
		Player ownerPlayer  = Bukkit.getPlayer(owner);
		if(ownerPlayer == null) return;
		saveTBParticles.cancel();
		/* Anvil Inv opening */
		TestBlockSlaveGUI.ChooseNameInv(ownerPlayer, chooseTB.getTier());
	}

	public void saveNewCustomTB(String name) {
		Player ownerPlayer  = Bukkit.getPlayer(owner);
	if(ownerPlayer == null) return;
		/* Save */
		name = name.replaceFirst(" ","");
		name = name.replace(" ", "_");
		MessageHandler msgHandler = MessageHandler.getInstance();

		boolean isValid = checkIfValid(name);
		if(!isValid) {
			msgHandler.send(ownerPlayer, "tbs_saveOwnTB_invalidName");
			msgHandler.send(ownerPlayer, "tbs_saveOwnTB_invalidLetters");
			return;
		}
		if (addNewCustomTestBlock(name)) {
			/* Message to player */
			msgHandler.send(ownerPlayer, "tbs_saveOwnTB_success", "" + newTBToSave.getTier(), name);
		}
	}

	private boolean checkIfValid(String name) {
		if(name.contains(".")) {
			return false;
		}
		if(name.startsWith(" ")&&name.length() == 1 || name.equals("")) {
			return false;
		}
		return CharMatcher.ascii().matchesAllOf(name);
	}

	public void showParticle() {
		Player ownerPlayer = Bukkit.getPlayer(owner);
		if(ownerPlayer == null) return;

		JsonCreater creator = new JsonCreater(MessageHandler.getInstance().getString(owner, "tbs_gui_confirmRegion"));
		JsonCreater click = new JsonCreater(MessageHandler.getInstance().getString(owner, "tbs_gui_confirmRegionConfirm"));
		click.addHoverEvent(MessageHandler.getInstance().getString(owner, "tbs_gui_confirmRegionHover"))
				.addClickEvent("/tbs confirmRegion " + owner, ClickAction.RUN_COMMAND);
		JsonCreater cancel = new JsonCreater(MessageHandler.getInstance().getString(owner, "tbs_gui_confirmRegionCancel"));
		cancel.addHoverEvent(MessageHandler.getInstance().getString(owner, "tbs_gui_confirmRegionCancelHover"))
				.addClickEvent("/tbs confirmRegionCancel " + owner, ClickAction.RUN_COMMAND);

		creator.addJson(click).addJson(cancel).send(ownerPlayer);

		Plot plot = BauPlayer.getBauPlayer(owner).getCurrentPlot();

		/* currentSelection: New_TB_TIER_FACING_TYPE */

		Facing facing = chooseTB.getFacing();
		int tier = chooseTB.getTier();
		Region rg = TestBlockSlaveCore.getTBRegion(tier, plot, facing);
		BlockVector3 min = rg.getMinimumPoint();
		BlockVector3 max = rg.getMaximumPoint();
		if (chooseTB.getType().equals(Type.SHIELDS)) {
			int shieldSize = TestBlockSlaveCore.getMaxShieldSizeOfTier(tier);
			min = min.subtract(shieldSize, 0, shieldSize);
			max = max.add(shieldSize, shieldSize, shieldSize);
		}
		BlockVector3 minVector = min;
		BlockVector3 maxVector = max;
		newTBToSave = new EmptyTestBlock(tier, new CuboidRegion(min, max), plot, facing, ownerPlayer.getWorld(),chooseTB.getType());
		saveTBParticles.cancel();
		saveTBParticles.setTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				TestBlockSlaveParticles.showTBParticlesShield(ownerPlayer, minVector, maxVector);
			}
		}, 0, 20));

	}

	public void setChooseTB(ChooseTestBlock chooseTB) {
		this.chooseTB = chooseTB;
	}

	public void cancelSave() {
		if (saveTBParticles.isRunning()) {
			saveTBParticles.cancel();
			MessageHandler.getInstance().send(owner, "tbs_save_canceled");
		} else {
			MessageHandler.getInstance().send(owner, "tbs_save_NoSaveToCancel");
		}
	}
}
