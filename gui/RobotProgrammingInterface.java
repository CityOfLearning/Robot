package com.dyn.robot.gui;

import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.dyn.DYNServerMod;
import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.server.network.NetworkManager;
import com.dyn.server.network.messages.MessageOpenRobotInterface;
import com.dyn.server.network.messages.MessageRunRobotScript;
import com.dyn.server.network.messages.MessageToggleRobotFollow;
import com.dyn.utils.FileUtils;
import com.google.common.collect.Lists;
import com.rabbit.gui.component.code.CodeInterface;
import com.rabbit.gui.component.control.Button;
import com.rabbit.gui.component.control.PictureButton;
import com.rabbit.gui.component.control.TextBox;
import com.rabbit.gui.component.display.Panel;
import com.rabbit.gui.component.display.Picture;
import com.rabbit.gui.component.display.TextLabel;
import com.rabbit.gui.component.display.tabs.CompassTab;
import com.rabbit.gui.component.display.tabs.ItemTab;
import com.rabbit.gui.component.display.tabs.PictureTab;
import com.rabbit.gui.component.display.tabs.Tab;
import com.rabbit.gui.component.list.DisplayList;
import com.rabbit.gui.component.list.ScrollableDisplayList;
import com.rabbit.gui.component.list.entries.ListEntry;
import com.rabbit.gui.component.list.entries.SelectElementEntry;
import com.rabbit.gui.component.list.entries.SelectListEntry;
import com.rabbit.gui.component.list.entries.SelectStringEntry;
import com.rabbit.gui.show.Show;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RobotProgrammingInterface extends Show {

	protected final EntityRobot robot;
	private String termText;
	private String fileName;

	private File[] files;
	private File selectedFile;

	private File currentDir;

	private CodeInterface codeWindow;
	private TextLabel openPath;

	private Panel errorPanel;
	private TextLabel errorLabel;
	private ScrollableDisplayList fileList;
	private TextBox fileNameEntry;

	private Path pathBase = Paths.get(Minecraft.getMinecraft().mcDataDir.getAbsolutePath());
	private Tab followTab;
	private Tab robotDirTab;
	private Tab playerDirTab;
	private Tab invTab;

	public RobotProgrammingInterface() {
		title = "Robot Programmer";
		termText = "#Welcome to the progamming interface!\n\nfrom api.ext.robot import *\n\nrobo = Robot()";

		currentDir = DYNServerMod.scriptsLoc;
		files = DYNServerMod.scriptsLoc.listFiles((FilenameFilter) (file, name) -> name.toLowerCase().endsWith(".py"));
		robot = null;
	}

	public RobotProgrammingInterface(EntityRobot robot) {
		title = "Robot Remote Interface";
		termText = "#Welcome to the progamming interface!\n\nfrom api.ext.robot import *\n\nrobo = Robot()";

		currentDir = DYNServerMod.scriptsLoc;
		files = DYNServerMod.scriptsLoc.listFiles((FilenameFilter) (file, name) -> name.toLowerCase().endsWith(".py"));
		this.robot = robot;
		if ((RobotMod.currentRobot == null) || (RobotMod.currentRobot != robot)) {
			RobotMod.currentRobot = robot;
		}
	}

	public void entrySelected(SelectListEntry entry, DisplayList dlist, int mouseX, int mouseY) {
		if (entry.getTitle().equals("..")) {
			dlist.clear();
			((ScrollableDisplayList) dlist).setScrollAmount(0);
			if (currentDir.getParentFile() != null) {
				currentDir = currentDir.getParentFile();
			}
			openPath.setText("Open File: " + pathBase.relativize(Paths.get(currentDir.getAbsolutePath())));
			files = currentDir.listFiles((FilenameFilter) (file, name) -> name.toLowerCase().endsWith(".py")
					|| new File(file, name).isDirectory());
			if (!pathBase.relativize(Paths.get(currentDir.getAbsolutePath())).equals(".")
					&& (currentDir.getParentFile() != null)) {
				// dont leave the minecraft directory
				dlist.add(new SelectStringEntry("..", (SelectStringEntry sentry, DisplayList sdlist, int smouseX,
						int smouseY) -> entrySelected(sentry, sdlist, smouseX, smouseY)));
			}
			for (File subfile : files) {
				dlist.add(new SelectElementEntry(subfile, subfile.getName(),
						(SelectElementEntry sentry, DisplayList sdlist, int smouseX, int smouseY) -> {
							entrySelected(sentry, sdlist, smouseX, smouseY);
						}));
			}

		} else {
			if (((File) entry.getValue()).isDirectory()) {
				((ScrollableDisplayList) dlist).setScrollAmount(0);
				currentDir = (File) entry.getValue();
				openPath.setText("Open File: " + pathBase.relativize(Paths.get(currentDir.getAbsolutePath())));
				dlist.clear();
				files = currentDir.listFiles((FilenameFilter) (file, name) -> name.toLowerCase().endsWith(".py")
						|| new File(file, name).isDirectory());
				dlist.add(new SelectStringEntry("..", (SelectStringEntry sentry, DisplayList sdlist, int smouseX,
						int smouseY) -> entrySelected(sentry, sdlist, smouseX, smouseY)));
				for (File subfile : files) {
					dlist.add(new SelectElementEntry(subfile, subfile.getName(),
							(SelectElementEntry sentry, DisplayList sdlist, int smouseX, int smouseY) -> {
								entrySelected(sentry, sdlist, smouseX, smouseY);
							}));
				}
			} else {
				selectedFile = (File) entry.getValue();
			}
		}
	}

	public String getConsoleText() {
		return codeWindow.getText();
	}

	public EntityRobot getRobot() {
		return robot;
	}
	
	@Override
	public void onClose() {
		super.onClose();
		RobotMod.proxy.toggleRenderRobotProgramInterface(true);
	}

	public void handleErrorMessage(String error, String code, int line) {
		// we have to subtract 2 since lines start at 0 and the error produces
		// an extra space
		errorPanel.setVisible(true);
		errorPanel.setFocused(true);
		errorLabel.setText(error);
		if (error.contains("NameError") || error.contains("RequestError") || error.contains("TypeError")) {
			// name errors dont seem to have the same offset as other errors
			codeWindow.notifyError(line - 1, code, error);
		} else {
			codeWindow.notifyError(line - 2, code, error);
		}
	}

	@Override
	public void setup() {
		super.setup();
		Panel mainPanel = new Panel((int) (width * .55), 0, (int) (width * .45), height).setFocused(true);

		Panel openPanel = new Panel((int) (width * .25), (int) (height * .23), (int) (width * .5), (int) (height * .55))
				.setVisible(false).setFocused(false).setZ(1000);

		registerComponent(openPanel);

		openPanel.registerComponent(new Picture(0, 0, (openPanel.getWidth()), (openPanel.getHeight()),
				new ResourceLocation("dyn", "textures/gui/background.png")));

		openPanel.registerComponent(openPath = new TextLabel(10, 10, openPanel.getWidth() - 20, 15, Color.black,
				"Open File: " + pathBase.relativize(Paths.get(currentDir.getAbsolutePath()))));

		// components
		ArrayList<ListEntry> scriptFiles = new ArrayList<>();

		scriptFiles.add(new SelectStringEntry("..", (SelectStringEntry entry, DisplayList dlist, int mouseX,
				int mouseY) -> entrySelected(entry, dlist, mouseX, mouseY)));
		for (File file : files) {
			scriptFiles.add(new SelectElementEntry(file, file.getName(), (SelectElementEntry entry, DisplayList dlist,
					int mouseX, int mouseY) -> entrySelected(entry, dlist, mouseX, mouseY)));
		}

		openPanel.registerComponent(fileList = new ScrollableDisplayList((int) (openPanel.getWidth() * .1),
				(int) (openPanel.getHeight() * .2), (int) (openPanel.getWidth() * .8),
				(int) (openPanel.getHeight() * .6), 15, scriptFiles));

		openPanel.registerComponent(
				new Button((int) (openPanel.getWidth() * .2), (int) (openPanel.getHeight() * .85), 45, 15, "Open")
						.setClickListener(btn -> {
							if (selectedFile != null) {
								termText = FileUtils.readFile(selectedFile);
								codeWindow.setText(termText);
								openPanel.setVisible(false);
								mainPanel.setFocused(true);
							}
						}));

		openPanel.registerComponent(
				new Button((int) (openPanel.getWidth() * .7), (int) (openPanel.getHeight() * .85), 45, 15, "Cancel")
						.setClickListener(btn -> {
							openPanel.setVisible(false);
							mainPanel.setFocused(true);
						}));

		Panel savePanel = new Panel((int) (width * .33), (int) (height * .33), (int) (width * .45),
				(int) (height * .33)).setVisible(false).setFocused(false).setZ(1000);

		registerComponent(savePanel);

		savePanel.registerComponent(new Picture(0, 0, (savePanel.getWidth()), (savePanel.getHeight()),
				new ResourceLocation("dyn", "textures/gui/background.png")));

		savePanel.registerComponent(new TextLabel((int) (savePanel.getWidth() * .1), (int) (savePanel.getHeight() * .1),
				(int) (savePanel.getWidth() * .8), 15, "Save File As:"));

		savePanel.registerComponent(fileNameEntry = new TextBox((int) (savePanel.getWidth() * .1),
				(int) (savePanel.getHeight() * .25), (int) (savePanel.getWidth() * .8), 15, "File Name")
						.setTextChangedListener((TextBox textbox, String previousText) -> {
							fileName = previousText;
						}));

		savePanel.registerComponent(
				new Button((savePanel.getWidth() / 3) - 23, savePanel.getHeight() - 35, 45, 15, "Save")
						.setClickListener(btn -> {
							if ((fileName != null) && !fileName.isEmpty()) {
								try {
									FileUtils.writeFile(new File(DYNServerMod.scriptsLoc, fileName + ".py"),
											Arrays.asList(termText.split(Pattern.quote("\n"))));
								} catch (Exception e) {
									DYNServerMod.logger.error("Could not create script file", e);
								}
							}
							fileNameEntry.setText("");
							fileName = "";
							savePanel.setVisible(false);
							mainPanel.setFocused(true);
						}));

		savePanel.registerComponent(
				new Button(((savePanel.getWidth() / 3) * 2) - 23, savePanel.getHeight() - 35, 45, 15, "Cancel")
						.setClickListener(btn -> {
							fileNameEntry.setText("");
							fileName = "";
							savePanel.setVisible(false);
							mainPanel.setFocused(true);
						}));

		registerComponent(mainPanel);

		mainPanel.registerComponent(robotDirTab = new CompassTab(0, 0, 50, 40, "Robot", 90, robot).setClickListener(tab -> {
			tab.setHidden(!tab.isHidden());
		}));

		mainPanel.registerComponent(playerDirTab = new CompassTab(0, 50, 50, 40, "Player", 90, Minecraft.getMinecraft().thePlayer)
				.setClickListener(tab -> {
					tab.setHidden(!tab.isHidden());
				}));

		mainPanel.registerComponent(invTab = new ItemTab(0, 100, 40, 40, "", 90, Blocks.chest)
				.setHoverText(Lists.newArrayList("Open Robot", "Inventory")).setDrawHoverText(true)
				.setClickListener(tab -> {
					NetworkManager.sendToServer(new MessageOpenRobotInterface(RobotMod.currentRobot.getEntityId()));
					RobotMod.proxy.toggleRenderRobotProgramInterface(true);
				}));

		mainPanel.registerComponent(followTab = new PictureTab(0, 140, 40, 40, "", 90,
				robot.getIsFollowing() ? new ResourceLocation("dyn", "textures/gui/robot_stand.png")
						: new ResourceLocation("dyn", "textures/gui/robot_follow.png"))
								.setHoverText(robot.getIsFollowing() ? Lists.newArrayList("Make Robot", "Stand still")
										: Lists.newArrayList("Make Robot", "Follow Me"))
								.setDrawHoverText(true).setClickListener(tab -> {
									if (!robot.getIsFollowing()) {
										NetworkManager.sendToServer(new MessageToggleRobotFollow(
												RobotMod.currentRobot.getEntityId(), true));
										((PictureTab) tab).setPicture(
												new ResourceLocation("dyn", "textures/gui/robot_stand.png"));
										tab.setHoverText(Lists.newArrayList("Make Robot", "Stand still"));
										robot.setIsFollowing(true);
									} else {
										NetworkManager.sendToServer(new MessageToggleRobotFollow(
												RobotMod.currentRobot.getEntityId(), false));
										((PictureTab) tab).setPicture(
												new ResourceLocation("dyn", "textures/gui/robot_follow.png"));
										tab.setHoverText(Lists.newArrayList("Make Robot", "Follow Me"));
										robot.setIsFollowing(false);
									}
								}));

		// The Panel background
		mainPanel.registerComponent(new Picture(0, 0, (mainPanel.getWidth()), (mainPanel.getHeight()),
				new ResourceLocation("dyn", "textures/gui/background2.png")));

		mainPanel.registerComponent(
				(codeWindow = new CodeInterface(10, 15, mainPanel.getWidth() - 20, mainPanel.getHeight() - 35))
						.setText(termText).setBackgroundVisibility(false).setDrawUnicode(true)
						.setTextChangedListener((TextBox textbox, String previousText) -> {
							List<String> codeLines = Lists.newArrayList();
							for (String line : previousText.split(Pattern.quote("\n"))) {
								line.trim();
								if (line.isEmpty() || (line.charAt(0) == '#') || line.contains("import")) {
									continue;
								}
								codeLines.add(line);
							}
							if (codeLines.size() > robot.getMemorySize()) {
								handleErrorMessage(
										"Robot out of memory, can only process " + robot.getMemorySize()
												+ " lines but program contains " + codeLines.size(),
										previousText.split(
												Pattern.quote("\n"))[previousText.split(Pattern.quote("\n")).length
														- 1],
										previousText.split(Pattern.quote("\n")).length);
								textbox.setText(termText);
							} else {
								termText = previousText;
							}
						}));

		// menu panel
		mainPanel.registerComponent(
				new Button(0, 0, (mainPanel.getWidth() - 15) / 4, 15, "<<Game").setClickListener(btn -> {
//					followTab.setHidden(true);
//					robotDirTab.setHidden(true);
//					playerDirTab.setHidden(true);
//					invTab.setHidden(true);
					RobotMod.proxy.toggleRenderRobotProgramInterface(true);
					Minecraft.getMinecraft().setIngameFocus();
				}));

		mainPanel.registerComponent(
				new Button((mainPanel.getWidth() - 15) / 4, 0, (mainPanel.getWidth() - 15) / 4, 15, "Open")
						.setClickListener(btn -> {
							files = DYNServerMod.scriptsLoc
									.listFiles((FilenameFilter) (file, name) -> name.toLowerCase().endsWith(".py"));
							fileList.clear();
							fileList.add(new SelectStringEntry("..", (SelectStringEntry entry, DisplayList dlist,
									int mouseX, int mouseY) -> entrySelected(entry, dlist, mouseX, mouseY)));
							for (File file : files) {
								fileList.add(new SelectElementEntry(file, file.getName(),
										(SelectElementEntry entry, DisplayList dlist, int mouseX,
												int mouseY) -> entrySelected(entry, dlist, mouseX, mouseY)));
							}
							openPanel.setVisible(true);
							openPanel.setFocused(true);
							mainPanel.setFocused(false);
						}).setIsEnabled(robot.robot_inventory.containsItem(new ItemStack(RobotMod.card, 1))));

		mainPanel.registerComponent(
				new Button((mainPanel.getWidth() - 15) / 2, 0, (mainPanel.getWidth() - 15) / 4, 15, "Save")
						.setClickListener(btn -> {
							savePanel.setVisible(true);
							savePanel.setFocused(true);
							mainPanel.setFocused(false);
						}).setIsEnabled(robot.robot_inventory.containsItem(new ItemStack(RobotMod.card, 1))));

		mainPanel.registerComponent(
				new Button(((mainPanel.getWidth() - 15) / 4) * 3, 0, (mainPanel.getWidth() - 15) / 4, 15, "Run")
						.setClickListener(btn -> {
							List<String> codeLines = Lists.newArrayList();
							for (String line : termText.split(Pattern.quote("\n"))) {
								line.trim();
								if (line.isEmpty() || (line.charAt(0) == '#') || line.contains("import")) {
									continue;
								}
								codeLines.add(line);
							}
							if (codeLines.size() > robot.getMemorySize()) {
								handleErrorMessage(
										"Robot out of memory, can only process " + robot.getMemorySize()
												+ " lines but program contains " + codeLines.size(),
										termText.split(Pattern.quote("\n"))[termText.split(Pattern.quote("\n")).length
												- 1],
										termText.split(Pattern.quote("\n")).length);
							} else {
								codeWindow.clearError();
								errorPanel.setVisible(false);
								NetworkManager
										.sendToServer(new MessageRunRobotScript(termText, robot.getEntityId(), true));
								((PictureTab) followTab)
										.setPicture(new ResourceLocation("dyn", "textures/gui/robot_follow.png"));
								followTab.setHoverText(Lists.newArrayList("Make Robot", "Follow Me"));
								robot.setIsFollowing(false);
							}
						}));

		mainPanel.registerComponent(new PictureButton(mainPanel.getWidth() - 15, 0, 15, 15,
				new ResourceLocation("dyn", "textures/gui/exit.png")).setDrawsButton(false).setClickListener(btn -> {
					RobotMod.proxy.toggleRenderRobotProgramInterface(false);
					Minecraft.getMinecraft().setIngameFocus();
				}));

		mainPanel.registerComponent(errorPanel = new Panel(0, (int) (mainPanel.getHeight() * .8), mainPanel.getWidth(),
				(int) (mainPanel.getHeight() * .2)).setVisible(false));

		errorPanel.registerComponent(new Picture(0, 0, (errorPanel.getWidth()), (errorPanel.getHeight()),
				new ResourceLocation("dyn", "textures/gui/background2.png")));

		errorPanel.registerComponent(
				errorLabel = new TextLabel(10, 20, errorPanel.getWidth() - 20, errorPanel.getHeight() - 20, "")
						.setMultilined(true));

		errorPanel.registerComponent(new TextLabel(10, 5, errorPanel.getWidth() - 20, 10, "Error Description:"));

		errorPanel.registerComponent(new PictureButton(mainPanel.getWidth() - 10, 0, 10, 10,
				new ResourceLocation("dyn", "textures/gui/exit.png")).setDrawsButton(false).setClickListener(btn -> {
					errorPanel.setVisible(false);
				}));

		List<String> robotMembers = Lists.newArrayList();
		robotMembers.add("forward()");
		robotMembers.add("backward()");
		robotMembers.add("inspect()");
		robotMembers.add("left()");
		robotMembers.add("right()");
		robotMembers.add("breakBlock()");
		robotMembers.add("placeBlock()");
		robotMembers.add("jump()");
		robotMembers.add("say()");
		robotMembers.add("interact()");

		codeWindow.addClassMembers("Robot", robotMembers);

	}

}
