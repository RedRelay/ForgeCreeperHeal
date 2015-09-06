package fr.eyzox.forgecreeperheal.healtimeline;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockBanner.BlockBannerHanging;
import net.minecraft.block.BlockBanner.BlockBannerStanding;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCake;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDragonEgg;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockTripWire;
import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.BlockWallSign;
import net.minecraft.util.BlockPos;
import fr.eyzox.forgecreeperheal.ForgeCreeperHeal;
import fr.eyzox.forgecreeperheal.healtimeline.factories.FacingRequirementFactory;
import fr.eyzox.forgecreeperheal.healtimeline.factories.IRequirementFactory;
import fr.eyzox.forgecreeperheal.healtimeline.factories.SupportByBottomRequirementFactory;
import fr.eyzox.forgecreeperheal.healtimeline.requirementcheck.IRequirementChecker;
import fr.eyzox.forgecreeperheal.worldhealer.WorldHealer;

public class HealTimeline extends AbstractCollection<BlockData>{
	
	private static LinkedList<IRequirementFactory> blockDataVisitorFactories = loadFactories();
	
	private static IRequirementChecker OnlyOneDependence = new IRequirementChecker() {
		@Override
		public boolean canBePlaced(Collection<BlockPos> blockPosCollection, BlockData data) {
			return true;
		}
	};
	
	private List<BlockData> canBePlaced = new ArrayList<BlockData>();
	private HashMap<BlockPos, LinkedList<BlockData>> waiting = new HashMap<BlockPos, LinkedList<BlockData>>();
	private Random rdn = new Random();
	private int tickLeftBeforeNextHeal;
	
	public HealTimeline(Collection<BlockData> blockdata) {
		this.addAll(blockdata);
		this.tickLeftBeforeNextHeal = ForgeCreeperHeal.getConfig().getMinimumTicksBeforeHeal() + ForgeCreeperHeal.getConfig().getRandomTickVar();
	}
	
	@Override
	public boolean add(BlockData data) {
		for(IRequirementFactory factory : blockDataVisitorFactories) {
			if(factory.accept(data)) {
				IRequirementChecker req = factory.getRequirementBase(data);
				if(req == null) {
					req = OnlyOneDependence;
				}
				data.setRequierement(req);
				return this.add(data, factory.getRequiredBlockPos(data));
			}
		}
		return add(data, null);
	}
	
	public boolean add(BlockData data, BlockPos[] requiredBlockPosTab) {
		if(requiredBlockPosTab == null || requiredBlockPosTab.length == 0) {
			canBePlaced.add(data);
		}else {
			for(BlockPos requiredBlockPos : requiredBlockPosTab) {
				LinkedList<BlockData> dependences = waiting.get(requiredBlockPos);
				if(dependences == null) {
					dependences = new LinkedList<BlockData>();
					waiting.put(requiredBlockPos, dependences);
				}
				dependences.add(data);
			}	
		}
		return true;
	}
	
	private BlockData advance(int i) {
		BlockData goingToBeHeal = canBePlaced.get(i);
		LinkedList<BlockData> dependences = waiting.remove(goingToBeHeal.getBlockPos());
		if(dependences == null) {
			canBePlaced.remove(i);
		}else {
			Iterator<BlockData> dependencesIterator = dependences.iterator();
			BlockData dependence = dependencesIterator.next();
			if(dependence.getRequierement().canBePlaced(waiting.keySet(), dependence)){
				canBePlaced.set(i, dependence);
			}
			
			while(dependencesIterator.hasNext()) {
				if(dependence.getRequierement().canBePlaced(waiting.keySet(), dependence)){
					canBePlaced.add(dependence);
				}
			}
		}
		
		return goingToBeHeal;
	}
	
	public static LinkedList<IRequirementFactory> getIBlockDataVisitorFactories() {
		return blockDataVisitorFactories;
	}
	
	public static IRequirementChecker getOnlyOneDependenceRequierement() {
		return OnlyOneDependence;
	}
	
	private static LinkedList<IRequirementFactory> loadFactories() {
		//TODO CHANGE NAME
		blockDataVisitorFactories = new LinkedList<IRequirementFactory>();
		blockDataVisitorFactories.add(new FacingRequirementFactory(BlockTorch.class, BlockTorch.FACING));
		blockDataVisitorFactories.add(new FacingRequirementFactory(BlockLadder.class, BlockLadder.FACING));
		blockDataVisitorFactories.add(new FacingRequirementFactory(BlockWallSign.class, BlockWallSign.FACING));
		blockDataVisitorFactories.add(new FacingRequirementFactory(BlockTrapDoor.class, BlockTrapDoor.FACING));
		blockDataVisitorFactories.add(new FacingRequirementFactory(BlockButton.class, BlockButton.FACING));
		blockDataVisitorFactories.add(new FacingRequirementFactory(BlockBannerHanging.class, BlockBannerHanging.FACING));
		blockDataVisitorFactories.add(new FacingRequirementFactory(BlockTripWireHook.class, BlockTripWireHook.FACING));
		//blockDataVisitorFactories.add(new FacingMatcherFactory(BlockPistonExtension.class, BlockPistonExtension.FACING));
		blockDataVisitorFactories.add(new SupportByBottomRequirementFactory(BlockFalling.class));
		blockDataVisitorFactories.add(new SupportByBottomRequirementFactory(BlockBasePressurePlate.class));
		blockDataVisitorFactories.add(new SupportByBottomRequirementFactory(BlockBannerStanding.class));
		blockDataVisitorFactories.add(new SupportByBottomRequirementFactory(BlockRedstoneDiode.class));
		blockDataVisitorFactories.add(new SupportByBottomRequirementFactory(BlockRedstoneWire.class));
		blockDataVisitorFactories.add(new SupportByBottomRequirementFactory(BlockStandingSign.class));
		blockDataVisitorFactories.add(new SupportByBottomRequirementFactory(BlockCrops.class));
		blockDataVisitorFactories.add(new SupportByBottomRequirementFactory(BlockCactus.class));
		blockDataVisitorFactories.add(new SupportByBottomRequirementFactory(BlockRailBase.class));
		blockDataVisitorFactories.add(new SupportByBottomRequirementFactory(BlockReed.class));
		blockDataVisitorFactories.add(new SupportByBottomRequirementFactory(BlockSnow.class));
		blockDataVisitorFactories.add(new SupportByBottomRequirementFactory(BlockTripWire.class));
		blockDataVisitorFactories.add(new SupportByBottomRequirementFactory(BlockCake.class));
		blockDataVisitorFactories.add(new SupportByBottomRequirementFactory(BlockCarpet.class));
		blockDataVisitorFactories.add(new SupportByBottomRequirementFactory(BlockDragonEgg.class));
		blockDataVisitorFactories.add(new SupportByBottomRequirementFactory(BlockFlowerPot.class));
		return blockDataVisitorFactories;
	}
	
	private class HealTimelineIterator implements Iterator<BlockData>{

		private Iterator<BlockData> itCursor = HealTimeline.this.canBePlaced.iterator();
		private Iterator<LinkedList<BlockData>> waitingIt = HealTimeline.this.waiting.values().iterator();
		private Collection<BlockData> dependencesCursor;
		
		@Override
		public boolean hasNext() {
			return itCursor.hasNext() || waitingIt.hasNext();
		}

		@Override
		public BlockData next() {
			if(!itCursor.hasNext()) {
				dependencesCursor = waitingIt.next();
				itCursor = dependencesCursor.iterator();
			}
			return itCursor.next();
		}

		@Override
		public void remove() {
			itCursor.remove();
			if(dependencesCursor != null && dependencesCursor.isEmpty()) {
				waitingIt.remove();
			}
		}
		
	}

	@Override
	public Iterator<BlockData> iterator() {
		return new HealTimelineIterator();
	}

	@Override
	public int size() {
		if(isEmpty()) return 0;
		int size = canBePlaced.size();
		for(Collection<BlockData> dependences : waiting.values()) {
			size += dependences.size();
		}
		return size;
	}
	
	public void onTick(WorldHealer healer) {
		tickLeftBeforeNextHeal--;
		if(tickLeftBeforeNextHeal < 0) {
			healer.heal(this.advance(rdn.nextInt(canBePlaced.size())));
			this.tickLeftBeforeNextHeal = ForgeCreeperHeal.getConfig().getRandomTickVar();
		}
	}
	
}