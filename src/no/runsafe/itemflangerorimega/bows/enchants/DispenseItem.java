package no.runsafe.itemflangerorimega.bows.enchants;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.block.IChest;
import no.runsafe.framework.api.entity.IEntity;
import no.runsafe.framework.api.entity.ILivingEntity;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.entity.RunsafeItem;
import no.runsafe.framework.minecraft.entity.RunsafeProjectile;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import no.runsafe.itemflangerorimega.bows.CustomBowEnchant;

import java.util.concurrent.ConcurrentHashMap;

public class DispenseItem extends CustomBowEnchant
{
	@Override
	public String getEnchantText()
	{
		return "Dispense Item I";
	}

	@Override
	public String getSimpleName()
	{
		return "dispense";
	}

	@Override
	public boolean onArrowShoot(ILivingEntity entity, IEntity arrow)
	{
		if (entity instanceof IPlayer)
		{
			IPlayer player = (IPlayer) entity;
			String playerName = player.getName();

			if (locations.containsKey(playerName))
			{
				ILocation chestLocation = locations.get(playerName);
				IBlock chestBlock = chestLocation.getBlock();

				if (chestBlock.is(Item.Decoration.Chest))
				{
					IChest chest = (IChest) chestBlock;
					RunsafeInventory chestInventory = chest.getInventory();

					RunsafeMeta item = chestInventory.getContents().get(0);
					chestInventory.remove(item);

					IWorld world = arrow.getWorld();
					if (world != null)
					{
						RunsafeItem itemEntity = world.dropItem(arrow.getLocation(), item);
						arrow.setPassenger(itemEntity);
					}
				}
			}
		}
		return true;
	}

	@Override
	public void onArrowCollideBlock(RunsafeProjectile projectile, IBlock block)
	{
		if (block.is(Item.Decoration.Chest))
		{
			IPlayer shooter = projectile.getShooterPlayer();
			if (shooter != null)
				locations.put(shooter.getName(), block.getLocation());
		}
	}

	private ConcurrentHashMap<String, ILocation> locations = new ConcurrentHashMap<String, ILocation>(0);
}