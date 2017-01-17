package com.dyn.robot.gui;

import java.awt.Color;

import com.dyn.DYNServerMod;
import com.dyn.robot.RobotMod;
import com.dyn.robot.blocks.BlockDynRobot;
import com.dyn.robot.entity.DynRobotEntity;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.server.network.NetworkManager;
import com.dyn.server.network.messages.MessageActivateRobot;
import com.dyn.server.network.messages.MessageDebugRobot;
import com.dyn.server.network.messages.MessageOpenRobotInventory;
import com.dyn.server.network.messages.MessageTeleportRobot;
import com.dyn.server.network.messages.MessageToggleRobotFollow;
import com.dyn.utils.PlayerAccessLevel;
import com.rabbit.gui.component.control.Button;
import com.rabbit.gui.component.control.CheckBox;
import com.rabbit.gui.component.control.PictureButton;
import com.rabbit.gui.component.control.TextBox;
import com.rabbit.gui.component.display.Panel;
import com.rabbit.gui.component.display.Picture;
import com.rabbit.gui.component.display.TextLabel;
import com.rabbit.gui.show.Show;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;

public class RemoteInterface extends Show {
	private BlockDynRobot robotBlock;
	private BlockPos robotBlockPos;
	private EntityPlayer player;
	private String robotName;

	public RemoteInterface(BlockDynRobot robot, EntityPlayer player, BlockPos pos) {
		robotBlock = robot;
		robotBlockPos = pos;
		this.player = player;
		robotName = "";
		if ((RobotMod.currentRobot != null) && RobotMod.currentRobot.isDead) {
			RobotMod.currentRobot = null;
		}
	}

	public RemoteInterface(EntityRobot robot, EntityPlayer player) {
		this.player = player;
		if ((RobotMod.currentRobot == null) || (RobotMod.currentRobot != robot)) {
			RobotMod.currentRobot = robot;
		}
	}

	@Override
	public void setup() {
		Panel debugPanel = new Panel((int) (width * .1), (int) (height * .1), (int) (width * .35), (int) (height * .6))
				.setVisible(false);

		debugPanel.registerComponent(new Picture(0, 0, debugPanel.getWidth(), debugPanel.getHeight(),
				new ResourceLocation("dyn", "textures/gui/background.png")));

		debugPanel.registerComponent(
				new TextLabel((int) (debugPanel.getWidth() * .05), (int) (debugPanel.getHeight() * .05),
						(int) (debugPanel.getWidth() * .8), 20, Color.black, "Robot Remote Debugger"));

		debugPanel.registerComponent(
				new Button((debugPanel.getWidth() / 4) - 15, (debugPanel.getHeight() / 2) - 15, 30, 30, "L")
						.setDoesDrawHoverText(true).addHoverText("Turn Left").setClickListener(btn -> {
							NetworkManager.sendToServer(new MessageDebugRobot(RobotMod.currentRobot.getEntityId(),
									MessageDebugRobot.CommandType.LEFT, 90));
						}));
		debugPanel.registerComponent(
				new Button((int) ((debugPanel.getWidth() * .75) - 15), (debugPanel.getHeight() / 4) - 15, 30, 30, "C")
						.setDoesDrawHoverText(true).addHoverText("Climb").setClickListener(btn -> {
							NetworkManager.sendToServer(new MessageDebugRobot(RobotMod.currentRobot.getEntityId(),
									MessageDebugRobot.CommandType.CLIMB, 1));
						}));
		// debugPanel.registerComponent(
		// new Button((int) ((debugPanel.getWidth() *.75) - 15),
		// (debugPanel.getHeight() / 4) - 15, 30, 30, "J")
		// .doesDrawHoverText(true).addHoverText("Jump").setClickListener(btn ->
		// {
		// NetworkDispatcher.sendToServer(new
		// MessageDebugRobot(RobotMod.currentRobot.getEntityId(),
		// MessageDebugRobot.CommandType.JUMP, 0));
		// }));
		debugPanel.registerComponent(
				new Button((debugPanel.getWidth() / 4) - 15, (debugPanel.getHeight() / 4) - 15, 30, 30, "I")
						.setDoesDrawHoverText(true).addHoverText("Interact With").setClickListener(btn -> {
							NetworkManager.sendToServer(new MessageDebugRobot(RobotMod.currentRobot.getEntityId(),
									MessageDebugRobot.CommandType.INTERACT, 0));
						}));
		debugPanel.registerComponent(
				new Button((debugPanel.getWidth() / 2) - 15, (debugPanel.getHeight() / 4) - 15, 30, 30, "F")
						.setDoesDrawHoverText(true).addHoverText("Move Forward").setClickListener(btn -> {
							NetworkManager.sendToServer(new MessageDebugRobot(RobotMod.currentRobot.getEntityId(),
									MessageDebugRobot.CommandType.FORWARD, 1));
						}));
		debugPanel.registerComponent(
				new Button((debugPanel.getWidth() / 2) - 15, (debugPanel.getHeight() / 2) - 15, 30, 30, "E")
						.setDoesDrawHoverText(true).addHoverText("Execute Program").setClickListener(btn -> {
							NetworkManager.sendToServer(new MessageDebugRobot(RobotMod.currentRobot.getEntityId(),
									MessageDebugRobot.CommandType.RUN, 0));
						}));
		debugPanel.registerComponent(
				new Button((int) ((debugPanel.getWidth() * .75) - 15), (debugPanel.getHeight() / 2) - 15, 30, 30, "R")
						.setDoesDrawHoverText(true).addHoverText("Turn Right").setClickListener(btn -> {
							NetworkManager.sendToServer(new MessageDebugRobot(RobotMod.currentRobot.getEntityId(),
									MessageDebugRobot.CommandType.RIGHT, 90));
						}));
		debugPanel.registerComponent(
				new Button((debugPanel.getWidth() / 4) - 15, (int) (debugPanel.getHeight() * .75) - 15, 30, 30, "P")
						.setDoesDrawHoverText(true).addHoverText("Place").setClickListener(btn -> {
							NetworkManager.sendToServer(new MessageDebugRobot(RobotMod.currentRobot.getEntityId(),
									MessageDebugRobot.CommandType.PLACE, 0));
						}));
		debugPanel.registerComponent(
				new Button((int) ((debugPanel.getWidth() * .75) - 15), (int) (debugPanel.getHeight() * .75) - 15, 30,
						30, "Br").setDoesDrawHoverText(true).addHoverText("Break").setClickListener(btn -> {
							NetworkManager.sendToServer(new MessageDebugRobot(RobotMod.currentRobot.getEntityId(),
									MessageDebugRobot.CommandType.BREAK, 0));
						}));
		debugPanel.registerComponent(
				new Button((debugPanel.getWidth() / 2) - 15, (int) ((debugPanel.getHeight() * .75) - 15), 30, 30, "B")
						.setDoesDrawHoverText(true).addHoverText("Move Back").setClickListener(btn -> {
							NetworkManager.sendToServer(new MessageDebugRobot(RobotMod.currentRobot.getEntityId(),
									MessageDebugRobot.CommandType.BACK, 1));
						}));

		Panel panel = new Panel((int) (width * .2), (int) (height * .2), (int) (width * .6), (int) (height * .6))
				.setVisible(true).setFocused(true);

		debugPanel.registerComponent(new PictureButton(debugPanel.getWidth() - 15, 0, 15, 15,
				new ResourceLocation("dyn", "textures/gui/exit.png")).setDrawsButton(false).setClickListener(btn -> {
					debugPanel.setVisible(false);
					panel.setVisible(true);
				}));

		panel.registerComponent(new TextLabel((int) (panel.getWidth() * .05), (int) (panel.getHeight() * .05),
				(int) (panel.getWidth() * .8), 20, Color.black, "Robot Remote"));

		if (RobotMod.currentRobot != null) {
			panel.registerComponent(new TextLabel((int) (panel.getWidth() * .05), (int) (panel.getHeight() * .15),
					(int) (panel.getWidth() * .8), panel.getHeight() / 2, Color.black,
					String.format("Your Current Robot:\nRobot Name: %s\nLocation:\n%s",
							RobotMod.currentRobot.getRobotName(), RobotMod.currentRobot.getPosition().toString()))
									.setMultilined(true));
			panel.registerComponent(new CheckBox((int) (panel.getWidth() * .65), (int) (panel.getHeight() * .05), 15,
					15, "is Following", RobotMod.currentRobot.getIsFollowing()).setStatusChangedListener(ckbx -> {
						NetworkManager.sendToServer(
								new MessageToggleRobotFollow(RobotMod.currentRobot.getEntityId(), ckbx.isChecked()));
						RobotMod.currentRobot.setIsFollowing(ckbx.isChecked());
					}));
		} else {
			panel.registerComponent(new TextBox((int) (panel.getWidth() * .1), (int) (panel.getHeight() * .2),
					(int) (panel.getWidth() * .8), 25, "Give Robot a Name")
							.setTextChangedListener((TextBox textbox, String previousText) -> {
								robotName = previousText;
							}));
		}

		if (robotBlock != null) {
			panel.registerComponent(new Button((int) (panel.getWidth() * .1), (int) (panel.getHeight() * .6),
					(int) (panel.getWidth() * .25), 20, "Activate").setClickListener(btn -> {
						if (RobotMod.currentRobot != null) {
							BlockPos pos = RobotMod.currentRobot.getPosition();
							NetworkManager.sendToServer(new MessageActivateRobot(player.getName(), pos,
									RobotMod.currentRobot.dimension, false));
							RobotMod.currentRobot.setDead();
							RobotMod.currentRobot = null;
						}
						NetworkManager.sendToServer(new MessageActivateRobot(
								(robotName.isEmpty() ? "Robot" + (int) (65535 * Math.random()) : robotName),
								robotBlockPos, player.dimension, true));
						getStage().close();
					}));
			if (RobotMod.currentRobot != null) {
				panel.registerComponent(new Button((int) (panel.getWidth() * .55), (int) (panel.getHeight() * .6),
						(int) (panel.getWidth() * .25), 20, "Deactivate").setClickListener(btn -> {
							BlockPos pos = RobotMod.currentRobot.getPosition();
							NetworkManager.sendToServer(new MessageActivateRobot(player.getName(), pos,
									RobotMod.currentRobot.dimension, false));
							RobotMod.currentRobot.setDead();
							RobotMod.currentRobot = null;
							Minecraft.getMinecraft().setIngameFocus();
						}));
			}
		} else if (RobotMod.currentRobot != null) {
			if (DYNServerMod.accessLevel == PlayerAccessLevel.ADMIN) {
				panel.registerComponent(new Button((int) (panel.getWidth() * .65), (int) (panel.getHeight() * .2),
						(int) (panel.getWidth() * .3), 20, "Debugger").setClickListener(btn -> {
							panel.setVisible(false);
							debugPanel.setVisible(true);
						}));
			}

			panel.registerComponent(new Button((int) (panel.getWidth() * .075), (int) (panel.getHeight() * .55),
					(int) (panel.getWidth() * .4), 20, "Open Programmer").setClickListener(btn -> {
						getStage().close();
						RobotMod.proxy.openRobotProgrammingWindow(RobotMod.currentRobot);
					}));

			panel.registerComponent(new Button((int) (panel.getWidth() * .55), (int) (panel.getHeight() * .55),
					(int) (panel.getWidth() * .35), 20, "Deactivate").setClickListener(btn -> {
						BlockPos pos = RobotMod.currentRobot.getPosition();
						NetworkManager.sendToServer(new MessageActivateRobot(player.getName(), pos,
								RobotMod.currentRobot.dimension, false));
						RobotMod.currentRobot.setDead();
						RobotMod.currentRobot = null;
						Minecraft.getMinecraft().setIngameFocus();
					}));

			panel.registerComponent(new Button((int) (panel.getWidth() * .5), (int) (panel.getHeight() * .75),
					(int) (panel.getWidth() * .45), 20, "Open Inventory").setClickListener(btn -> {
						NetworkManager.sendToServer(new MessageOpenRobotInventory(RobotMod.currentRobot.getEntityId()));
					}));

			panel.registerComponent(new Button((int) (panel.getWidth() * .1), (int) (panel.getHeight() * .75),
					(int) (panel.getWidth() * .35), 20, "Teleport to Me").setClickListener(btn -> {
						NetworkManager.sendToServer(new MessageTeleportRobot(RobotMod.currentRobot.getEntityId()));
						((DynRobotEntity) RobotMod.currentRobot).spawnParticles(EnumParticleTypes.REDSTONE);
					}));
		}

		panel.registerComponent(new Picture(0, 0, panel.getWidth(), panel.getHeight(),
				new ResourceLocation("dyn", "textures/gui/background.png")));

		// this is my lazy way of not having to rearrange things
		panel.reverseComponents();

		registerComponent(panel);
		registerComponent(debugPanel);
	}
}
