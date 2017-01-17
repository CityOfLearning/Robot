package com.dyn.robot;

import java.util.Map;

import com.dyn.DYNServerMod;
import com.dyn.fixins.tab.RoboTab;
import com.dyn.robot.api.RobotAPI;
import com.dyn.robot.blocks.BlockDynRobot;
import com.dyn.robot.entity.DynRobotEntity;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.items.ItemDynRobotBlock;
import com.dyn.robot.items.ItemDynRobotSpawner;
import com.dyn.robot.items.ItemRemote;
import com.dyn.robot.items.ItemWrench;
import com.dyn.robot.proxy.Proxy;
import com.dyn.robot.reference.MetaData;
import com.dyn.robot.reference.Reference;
import com.dyn.utils.PlayerAccessLevel;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, dependencies = "required-after:dyn|server")
public class RobotMod {
	@Mod.Instance(Reference.MOD_ID)
	public static RobotMod instance;

	@SidedProxy(modId = Reference.MOD_ID, clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static Proxy proxy;

	public static BlockDynRobot dynRobot;
	public static ItemRemote dynRobotRemote;
	public static ItemWrench dynRobotWrench;
	public static ItemDynRobotSpawner robotSpawner;

	public static CreativeTabs roboTab = new RoboTab();

	// server
	public static Map<Integer, Boolean> robotEcho = Maps.newHashMap();
	public static BiMap<Integer, EntityPlayer> robotid2player = HashBiMap.create();

	// client
	public static EntityRobot currentRobot;

	public static void registerNewEntity(Class entityClass, String name, int id) {
		EntityRegistry.registerModEntity(entityClass, name, id, instance, 64, 3, false);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init();
		proxy.registerBlockItem(dynRobot);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MetaData.init(event.getModMetadata());

		registerItems();
		registerBlocks();

		registerNewEntity(DynRobotEntity.class, "dynRobotEntity", 0);

		proxy.preInit();
	}

	private void registerBlocks() {
		dynRobot = (BlockDynRobot) new BlockDynRobot().setUnlocalizedName("dyn_robot").setCreativeTab(roboTab);
		GameRegistry.registerBlock(dynRobot, ItemDynRobotBlock.class, "dyn_robot");
	}

	private void registerItems() {
		dynRobotRemote = (ItemRemote) new ItemRemote().setUnlocalizedName("dyn_robot_remote").setCreativeTab(roboTab);
		GameRegistry.registerItem(dynRobotRemote, "dyn_robot_remote");
		proxy.registerItem(dynRobotRemote, dynRobotRemote.getUnlocalizedName(), 0);

		dynRobotWrench = (ItemWrench) new ItemWrench().setUnlocalizedName("dyn_robot_wrench");
		GameRegistry.registerItem(dynRobotWrench, "dyn_robot_wrench");
		proxy.registerItem(dynRobotWrench, dynRobotWrench.getUnlocalizedName(), 0);

		robotSpawner = (ItemDynRobotSpawner) new ItemDynRobotSpawner().setUnlocalizedName("dyn_robot_spawn");
		GameRegistry.registerItem(robotSpawner, "dyn_robot_spawn");
		proxy.registerItem(robotSpawner, robotSpawner.getUnlocalizedName() + "_plus", 0);
		proxy.registerItem(robotSpawner, robotSpawner.getUnlocalizedName(), 1);

		if (DYNServerMod.developmentEnvironment || (DYNServerMod.accessLevel != PlayerAccessLevel.STUDENT)) {
			dynRobotWrench.setCreativeTab(roboTab);
			robotSpawner.setCreativeTab(roboTab);
		}
	}

	// this only fires for the client if its internal
	@Mod.EventHandler
	public void serverStarted(FMLServerStartingEvent e) {
		if (e.getSide() == Side.CLIENT) {
			try {
				Class.forName("mobi.omegacentauri.raspberryjammod.RaspberryJamMod");
				RobotAPI.registerCommands();
			} catch (ClassNotFoundException er) {
				// this is just to make sure rjm exists
			}
		}
	}
}
