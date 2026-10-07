package com.prestige92;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
		name = "Prestige 92"
)
public class Prestige92Plugin extends Plugin
{
	private static final int LEVEL_92_XP =
			6517253;

	private static final int LEVEL_99_XP =
			13034431;

	private static final String CONFIG_GROUP =
			"prestige92";

	private static final String HEADING_ICON_KEY =
			"useIconHeading";

	private final Map<Skill, Integer> previousPrestigeLevels =
			new EnumMap<>(Skill.class);

	private final Map<Skill, Integer> previousRealXp =
			new EnumMap<>(Skill.class);

	@Inject
	private Client client;

	@Inject
	private Prestige92Config config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Prestige92LevelUpOverlay levelUpOverlay;

	@Inject
	private ClientThread clientThread;

	private Prestige92Panel panel;

	private NavigationButton navButton;

	private boolean sessionInitialised =
			false;

	@Override
	protected void startUp()
	{
		BufferedImage icon =
				ImageUtil.loadImageResource(
						Prestige92Plugin.class,
						"/prestige92.png"
				);

		panel =
				new Prestige92Panel(
						icon,
						config
				);

		panel.setColourSaveAction(
				this::saveSkillColour
		);

		panel.setColourLoadAction(
				this::loadSkillColour
		);

		panel.setColourResetAction(
				this::resetSkillColour
		);

		panel.setHeadingSaveAction(
				this::saveHeadingStyle
		);

		panel.setUseIconHeading(
				config.useIconHeading()
		);

		navButton =
				NavigationButton.builder()
						.tooltip(
								"Prestige 92"
						)
						.icon(
								icon
						)
						.priority(
								5
						)
						.panel(
								panel
						)
						.build();

		clientToolbar.addNavigation(
				navButton
		);

		overlayManager.add(
				levelUpOverlay
		);

		/*
		 * If the plugin is enabled while already
		 * logged in, initialise immediately.
		 */
		clientThread.invokeLater(
				() ->
				{
					if (
							client.getGameState()
									== GameState.LOGGED_IN
					)
					{
						initialiseSession();
					}
				}
		);
	}

	@Override
	protected void shutDown()
	{
		if (
				navButton != null
		)
		{
			clientToolbar.removeNavigation(
					navButton
			);
		}

		overlayManager.remove(
				levelUpOverlay
		);

		levelUpOverlay.hide();

		previousPrestigeLevels.clear();

		previousRealXp.clear();

		sessionInitialised =
				false;

		navButton =
				null;

		panel =
				null;
	}

	/*
	 * Proper login/logout handling.
	 *
	 * This prevents login stat synchronisation from
	 * being mistaken for actual training XP.
	 */
	@Subscribe
	public void onGameStateChanged(
			GameStateChanged event
	)
	{
		GameState gameState =
				event.getGameState();

		if (
				gameState == GameState.LOGGED_IN
		)
		{
			initialiseSession();

			return;
		}

		if (
				gameState == GameState.LOGIN_SCREEN
						|| gameState
						== GameState.HOPPING
		)
		{
			sessionInitialised =
					false;

			previousRealXp.clear();

			previousPrestigeLevels.clear();

			SwingUtilities.invokeLater(
					() ->
					{
						if (
								panel != null
						)
						{
							panel.clearAllSkills();
						}
					}
			);
		}
	}

	/*
	 * React to RuneLite configuration changes
	 * immediately.
	 */
	@Subscribe
	public void onConfigChanged(
			ConfigChanged event
	)
	{
		if (
				!CONFIG_GROUP.equals(
						event.getGroup()
				)
		)
		{
			return;
		}

		if (
				HEADING_ICON_KEY.equals(
						event.getKey()
				)
		)
		{
			SwingUtilities.invokeLater(
					() ->
					{
						if (
								panel != null
						)
						{
							panel.setUseIconHeading(
									config.useIconHeading()
							);
						}
					}
			);
		}

		if (
				"defaultProgressColour".equals(
						event.getKey()
				)
		)
		{
			SwingUtilities.invokeLater(
					() ->
					{
						if (
								panel != null
						)
						{
							panel.refreshDefaultColours();
						}
					}
			);
		}
	}

	private void initialiseSession()
	{
		if (
				client.getGameState()
						!= GameState.LOGGED_IN
		)
		{
			return;
		}

		previousRealXp.clear();

		previousPrestigeLevels.clear();

		for (
				Skill skill
				: Skill.values()
		)
		{
			if (
					skill == Skill.OVERALL
			)
			{
				continue;
			}

			int realXp =
					getRealXp(
							skill
					);

			previousRealXp.put(
					skill,
					realXp
			);

			if (
					realXp >= LEVEL_92_XP
			)
			{
				int prestigeXp =
						getPrestigeXpFromRealXp(
								realXp
						);

				int prestigeLevel =
						getPrestigeLevel(
								prestigeXp
						);

				previousPrestigeLevels.put(
						skill,
						prestigeLevel
				);
			}
		}

		sessionInitialised =
				true;

		SwingUtilities.invokeLater(
				() ->
				{
					if (
							panel != null
					)
					{
						panel.clearAllSkills();
					}
				}
		);
	}

	private int getRealXp(
			Skill skill
	)
	{
		return client.getSkillExperience(
				skill
		);
	}

	private int getPrestigeXp(
			Skill skill
	)
	{
		return getPrestigeXpFromRealXp(
				getRealXp(
						skill
				)
		);
	}

	/*
	 * This keeps the Prestige maths exactly as before.
	 */
	private int getPrestigeXpFromRealXp(
			int realXp
	)
	{
		if (
				realXp <= LEVEL_92_XP
		)
		{
			return 0;
		}

		int xpAfter92 =
				realXp
						- LEVEL_92_XP;

		int xpFrom92To99 =
				LEVEL_99_XP
						- LEVEL_92_XP;

		return Math.min(
				LEVEL_92_XP,
				(int) (
						(long) xpAfter92
								* LEVEL_92_XP
								/ xpFrom92To99
				)
		);
	}

	private int getPrestigeLevel(
			int prestigeXp
	)
	{
		for (
				int level = 92;
				level >= 1;
				level--
		)
		{
			if (
					prestigeXp
							>= Experience.getXpForLevel(
							level
					)
			)
			{
				return level;
			}
		}

		return 1;
	}

	/*
	 * Convert a target Prestige XP value back into
	 * the REAL RuneScape XP required to reach it.
	 */
	private int getRealXpForPrestigeXp(
			int prestigeXp
	)
	{
		if (
				prestigeXp <= 0
		)
		{
			return LEVEL_92_XP;
		}

		long realXpRange =
				LEVEL_99_XP
						- LEVEL_92_XP;

		long numerator =
				(long) prestigeXp
						* realXpRange;

		long scaledXp =
				(
						numerator
								+ LEVEL_92_XP
								- 1
				)
						/ LEVEL_92_XP;

		long result =
				LEVEL_92_XP
						+ scaledXp;

		return (int) Math.min(
				LEVEL_99_XP,
				result
		);
	}

	private int getRealXpRemaining(
			int realXp,
			int prestigeLevel
	)
	{
		if (
				prestigeLevel >= 92
		)
		{
			return 0;
		}

		int nextPrestigeXp =
				Experience.getXpForLevel(
						prestigeLevel + 1
				);

		int targetRealXp =
				getRealXpForPrestigeXp(
						nextPrestigeXp
				);

		return Math.max(
				0,
				targetRealXp
						- realXp
		);
	}

	@Subscribe
	public void onStatChanged(
			StatChanged event
	)
	{
		if (
				!sessionInitialised
		)
		{
			return;
		}

		Skill skill =
				event.getSkill();

		if (
				skill == Skill.OVERALL
		)
		{
			return;
		}

		int realXp =
				getRealXp(
						skill
				);

		Integer oldRealXp =
				previousRealXp.put(
						skill,
						realXp
				);

		if (
				oldRealXp == null
		)
		{
			initialiseSkillState(
					skill,
					realXp
			);

			return;
		}

		/*
		 * Only genuine XP increases count.
		 */
		if (
				realXp <= oldRealXp
		)
		{
			return;
		}

		/*
		 * The skill must have reached real level 92.
		 */
		if (
				realXp < LEVEL_92_XP
		)
		{
			return;
		}

		int prestigeXp =
				getPrestigeXpFromRealXp(
						realXp
				);

		int prestigeLevel =
				getPrestigeLevel(
						prestigeXp
				);

		int realXpRemaining =
				getRealXpRemaining(
						realXp,
						prestigeLevel
				);

		SwingUtilities.invokeLater(
				() ->
				{
					if (
							panel != null
					)
					{
						panel.updateSkill(
								skill,
								prestigeXp,
								prestigeLevel,
								realXpRemaining
						);
					}
				}
		);

		Integer previousLevel =
				previousPrestigeLevels.put(
						skill,
						prestigeLevel
				);

		if (
				previousLevel != null
						&& prestigeLevel
						> previousLevel
		)
		{
			showPrestigeLevelUp(
					skill,
					prestigeLevel
			);
		}
	}

	private void initialiseSkillState(
			Skill skill,
			int realXp
	)
	{
		previousRealXp.put(
				skill,
				realXp
		);

		if (
				realXp < LEVEL_92_XP
		)
		{
			return;
		}

		int prestigeXp =
				getPrestigeXpFromRealXp(
						realXp
				);

		int prestigeLevel =
				getPrestigeLevel(
						prestigeXp
				);

		previousPrestigeLevels.put(
				skill,
				prestigeLevel
		);
	}

	private void showPrestigeLevelUp(
			Skill skill,
			int prestigeLevel
	)
	{
		boolean completed =
				prestigeLevel >= 92;

		if (
				config.chatMessageEnabled()
		)
		{
			String message;

			if (
					completed
			)
			{
				message =
						"Prestige Complete! "
								+ skill.getName()
								+ " has reached Prestige 92 and level 99!";
			}
			else
			{
				message =
						"Congratulations! You've reached Prestige level "
								+ prestigeLevel
								+ " in "
								+ skill.getName()
								+ "!";
			}

			client.addChatMessage(
					ChatMessageType.GAMEMESSAGE,
					"",
					message,
					null
			);
		}

		if (
				config.fireworksEnabled()
		)
		{
			levelUpOverlay.showLevelUp(
					skill,
					prestigeLevel
			);
		}

		if (
				config.soundEnabled()
		)
		{
			playPrestigeLevelUpSound();
		}
	}

	private void saveSkillColour(
			Skill skill,
			Color colour
	)
	{
		String key =
				getSkillColourKey(
						skill
				);

		String value =
				String.format(
						"%d,%d,%d",
						colour.getRed(),
						colour.getGreen(),
						colour.getBlue()
				);

		configManager.setConfiguration(
				CONFIG_GROUP,
				key,
				value
		);
	}

	private Color loadSkillColour(
			Skill skill
	)
	{
		String value =
				configManager.getConfiguration(
						CONFIG_GROUP,
						getSkillColourKey(
								skill
						)
				);

		if (
				value == null
						|| value.isEmpty()
		)
		{
			return null;
		}

		try
		{
			String[] parts =
					value.split(
							","
					);

			if (
					parts.length != 3
			)
			{
				return null;
			}

			return new Color(
					Integer.parseInt(
							parts[0]
					),
					Integer.parseInt(
							parts[1]
					),
					Integer.parseInt(
							parts[2]
					)
			);
		}
		catch (
				Exception exception
		)
		{
			log.debug(
					"Could not load colour for {}",
					skill,
					exception
			);

			return null;
		}
	}

	private void resetSkillColour(
			Skill skill
	)
	{
		configManager.unsetConfiguration(
				CONFIG_GROUP,
				getSkillColourKey(
						skill
				)
		);
	}

	private String getSkillColourKey(
			Skill skill
	)
	{
		return skill.name()
				.toLowerCase()
				+ "Colour";
	}

	private void saveHeadingStyle(
			boolean useIcon
	)
	{
		configManager.setConfiguration(
				CONFIG_GROUP,
				HEADING_ICON_KEY,
				Boolean.toString(
						useIcon
				)
		);
	}

	/*
	 * Sound loading now uses getResourceAsStream().
	 *
	 * This is preferable for Plugin Hub deployment
	 * because the resource will live inside the
	 * plugin JAR.
	 */
	private void playPrestigeLevelUpSound()
	{
		final int requestedVolume =
				Math.max(
						0,
						Math.min(
								100,
								config.soundVolume()
						)
				);

		Thread soundThread =
				new Thread(
						() ->
						{
							InputStream rawStream =
									null;

							BufferedInputStream bufferedStream =
									null;

							AudioInputStream audioStream =
									null;

							SourceDataLine audioLine =
									null;

							try
							{
								rawStream =
										Prestige92Plugin.class
												.getResourceAsStream(
														"/prestige_level_up.wav"
												);

								if (
										rawStream == null
								)
								{
									log.error(
											"Prestige 92 sound resource could not be found"
									);

									return;
								}

								bufferedStream =
										new BufferedInputStream(
												rawStream
										);

								audioStream =
										AudioSystem.getAudioInputStream(
												bufferedStream
										);

								AudioFormat format =
										audioStream.getFormat();

								DataLine.Info lineInfo =
										new DataLine.Info(
												SourceDataLine.class,
												format
										);

								if (
										!AudioSystem.isLineSupported(
												lineInfo
										)
								)
								{
									log.error(
											"Prestige 92 audio format is unsupported: {}",
											format
									);

									return;
								}

								audioLine =
										(SourceDataLine)
												AudioSystem.getLine(
														lineInfo
												);

								audioLine.open(
										format
								);

								/*
								 * Use the audio line's native MASTER_GAIN
								 * control when available.
								 */
								if (
										audioLine.isControlSupported(
												FloatControl.Type.MASTER_GAIN
										)
								)
								{
									FloatControl gainControl =
											(FloatControl)
													audioLine.getControl(
															FloatControl.Type.MASTER_GAIN
													);

									float volumeFraction =
											requestedVolume
													/ 100.0f;

									float gain;

									if (
											volumeFraction <= 0.0f
									)
									{
										gain =
												gainControl.getMinimum();
									}
									else
									{
										gain =
												(float) (
														20.0
																* Math.log10(
																volumeFraction
														)
												);

										gain =
												Math.max(
														gainControl.getMinimum(),
														Math.min(
																gainControl.getMaximum(),
																gain
														)
												);
									}

									gainControl.setValue(
											gain
									);
								}

								audioLine.start();

								byte[] buffer =
										new byte[4096];

								int bytesRead;

								while (
										(
												bytesRead =
														audioStream.read(
																buffer
														)
										) != -1
								)
								{
									audioLine.write(
											buffer,
											0,
											bytesRead
									);
								}

								audioLine.drain();
							}
							catch (
									Exception exception
							)
							{
								log.error(
										"Error playing Prestige 92 sound",
										exception
								);
							}
							finally
							{
								if (
										audioLine != null
								)
								{
									try
									{
										audioLine.stop();
									}
									catch (
											Exception ignored
									)
									{
									}

									audioLine.close();
								}

								if (
										audioStream != null
								)
								{
									try
									{
										audioStream.close();
									}
									catch (
											Exception ignored
									)
									{
									}
								}
								else if (
										bufferedStream != null
								)
								{
									try
									{
										bufferedStream.close();
									}
									catch (
											Exception ignored
									)
									{
									}
								}
								else if (
										rawStream != null
								)
								{
									try
									{
										rawStream.close();
									}
									catch (
											Exception ignored
									)
									{
									}
								}
							}
						}
				);

		soundThread.setName(
				"Prestige-92-Sound"
		);

		soundThread.setDaemon(
				true
		);

		soundThread.start();
	}

	@Provides
	Prestige92Config provideConfig(
			ConfigManager configManager
	)
	{
		return configManager.getConfig(
				Prestige92Config.class
		);
	}
}