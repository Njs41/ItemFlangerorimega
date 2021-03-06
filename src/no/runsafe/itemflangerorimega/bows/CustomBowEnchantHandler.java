package no.runsafe.itemflangerorimega.bows;

import no.runsafe.framework.api.entity.IEntity;
import no.runsafe.framework.api.entity.ILivingEntity;
import no.runsafe.framework.api.event.entity.IEntityDamageByEntityEvent;
import no.runsafe.framework.api.event.entity.IEntityShootBowEvent;
import no.runsafe.framework.api.event.entity.IProjectileHitEvent;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.entity.ProjectileEntity;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import no.runsafe.framework.minecraft.entity.RunsafeProjectile;
import no.runsafe.framework.minecraft.event.entity.RunsafeEntityDamageByEntityEvent;
import no.runsafe.framework.minecraft.event.entity.RunsafeEntityShootBowEvent;
import no.runsafe.framework.minecraft.event.entity.RunsafeProjectileHitEvent;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomBowEnchantHandler implements IProjectileHitEvent, IEntityShootBowEvent, IEntityDamageByEntityEvent
{
	public CustomBowEnchantHandler(ICustomBowEnchant[] enchants)
	{
		this.enchants = Arrays.asList(enchants);
		for (ICustomBowEnchant enchant : this.enchants)
			this.enchantMap.put(enchant.getSimpleName(), enchant);
	}

	@Override
	public void OnEntityDamageByEntity(RunsafeEntityDamageByEntityEvent event)
	{
		if (event.getDamageActor() instanceof RunsafeProjectile)
		{
			RunsafeProjectile projectile = (RunsafeProjectile) event.getDamageActor();
			if (projectile.getEntityType() == ProjectileEntity.Arrow && this.isTrackedArrow(projectile))
			{
				List<ICustomBowEnchant> arrowEnchants = this.trackedArrows.get(projectile.getEntityId());
				for (ICustomBowEnchant enchant : arrowEnchants)
					enchant.onArrowCollideEntity(projectile, event.getEntity());
			}
		}
	}

	@Override
	public void OnProjectileHit(RunsafeProjectileHitEvent event)
	{
		RunsafeProjectile projectile = event.getProjectile();
		if (this.isTrackedArrow(projectile))
		{
			List<ICustomBowEnchant> arrowEnchants = this.trackedArrows.get(projectile.getEntityId());
			for (ICustomBowEnchant enchant : arrowEnchants)
			{
				enchant.onArrowCollide(projectile);
				if (projectile.isOnGround())
					enchant.onArrowCollideBlock(projectile, projectile.getImpaledBlock());
			}
			this.unTrackProjectile(projectile);
		}
	}

	private void unTrackProjectile(RunsafeProjectile projectile)
	{
		this.trackedArrows.remove(projectile.getEntityId());
	}

	private boolean isTrackedArrow(RunsafeProjectile projectile)
	{
		return this.trackedArrows.containsKey(projectile.getEntityId());
	}

	private boolean hasEnchant(RunsafeMeta item, ICustomBowEnchant enchant)
	{
		List<String> lore = item.getLore();
		return lore != null && lore.contains("§r§7" + enchant.getEnchantText());
	}

	public void enchantBow(RunsafeMeta item, ICustomBowEnchant enchant)
	{
		if (!this.hasEnchant(item, enchant))
			item.addLore("§r§7" + enchant.getEnchantText());
	}

	public ICustomBowEnchant getEnchant(String name)
	{
		return this.enchantMap.containsKey(name) ? this.enchantMap.get(name) : null;
	}

	public Set<String> getAvailableEnchants()
	{
		return this.enchantMap.keySet();
	}

	@Override
	public void OnEntityShootBowEvent(RunsafeEntityShootBowEvent event)
	{
		int entityID = event.getProjectile().getEntityId();

		if (!trackedArrows.containsKey(entityID))
		{
			RunsafeEntity shootingEntity = event.getEntity();

			RunsafeMeta item = null;
			if (shootingEntity instanceof IPlayer)
				item = ((IPlayer) shootingEntity).getItemInHand();
			else if (shootingEntity instanceof ILivingEntity)
				item = ((ILivingEntity) shootingEntity).getEquipment().getItemInHand();

			if (item != null && item.is(Item.Combat.Bow))
			{
				List<ICustomBowEnchant> bowEnchants = new ArrayList<ICustomBowEnchant>();
				for (ICustomBowEnchant enchant : enchants)
				{
					if (hasEnchant(item, enchant))
					{
						boolean allowShoot = enchant.onArrowShoot((ILivingEntity) shootingEntity, (IEntity) event.getProjectile());

						if (allowShoot)
							bowEnchants.add(enchant);
						else
							event.getProjectile().remove();
					}
				}

				if (!bowEnchants.isEmpty())
					trackedArrows.put(entityID, bowEnchants);
			}
		}
	}

	private ConcurrentHashMap<Integer, List<ICustomBowEnchant>> trackedArrows = new ConcurrentHashMap<Integer, List<ICustomBowEnchant>>();
	private HashMap<String, ICustomBowEnchant> enchantMap = new HashMap<String, ICustomBowEnchant>();
	private List<ICustomBowEnchant> enchants = new ArrayList<ICustomBowEnchant>();
}
