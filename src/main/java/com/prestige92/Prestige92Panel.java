package com.prestige92;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.runelite.api.Experience;
import net.runelite.api.Skill;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class Prestige92Panel extends PluginPanel
{
    private static final Color PRESTIGE_GOLD =
            new Color(
                    255,
                    190,
                    45
            );

    private static final Color COMPLETION_GOLD =
            new Color(
                    255,
                    215,
                    0
            );

    private final Prestige92Config config;

    private final JPanel skillsPanel;

    private final Map<Skill, SkillDisplay> skillDisplays =
            new EnumMap<>(Skill.class);

    private final Map<Skill, Color> skillColours =
            new EnumMap<>(Skill.class);

    private final Map<Skill, Boolean> customColours =
            new EnumMap<>(Skill.class);

    private final JLabel titleLabel;

    private final ImageIcon titleIcon;

    private boolean useIconHeading = true;

    private BiConsumer<Skill, Color> colourSaveAction;

    private Function<Skill, Color> colourLoadAction;

    private Consumer<Skill> colourResetAction;

    private Consumer<Boolean> headingSaveAction;

    public Prestige92Panel(
            BufferedImage pluginIcon,
            Prestige92Config config
    )
    {
        this.config =
                config;

        setLayout(
                new BorderLayout()
        );

        setBackground(
                ColorScheme.DARK_GRAY_COLOR
        );

        Image scaledIcon =
                pluginIcon.getScaledInstance(
                        42,
                        42,
                        Image.SCALE_SMOOTH
                );

        titleIcon =
                new ImageIcon(
                        scaledIcon
                );

        JPanel mainPanel =
                new JPanel();

        mainPanel.setLayout(
                new BoxLayout(
                        mainPanel,
                        BoxLayout.Y_AXIS
                )
        );

        mainPanel.setBackground(
                ColorScheme.DARK_GRAY_COLOR
        );

        mainPanel.setBorder(
                new EmptyBorder(
                        12,
                        8,
                        12,
                        8
                )
        );

        titleLabel =
                new JLabel(
                        "Prestige 92"
                );

        titleLabel.setAlignmentX(
                CENTER_ALIGNMENT
        );

        titleLabel.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        titleLabel.setFont(
                titleLabel
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                18f
                        )
        );

        titleLabel.setForeground(
                PRESTIGE_GOLD
        );

        titleLabel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        48
                )
        );

        installHeadingMenu(
                titleLabel
        );

        mainPanel.add(
                titleLabel
        );

        mainPanel.add(
                Box.createVerticalStrut(
                        12
                )
        );

        skillsPanel =
                new JPanel();

        skillsPanel.setLayout(
                new BoxLayout(
                        skillsPanel,
                        BoxLayout.Y_AXIS
                )
        );

        skillsPanel.setBackground(
                ColorScheme.DARK_GRAY_COLOR
        );

        mainPanel.add(
                skillsPanel
        );

        mainPanel.add(
                Box.createVerticalGlue()
        );

        installClearAllMenu(
                mainPanel
        );

        installClearAllMenu(
                skillsPanel
        );

        JScrollPane scrollPane =
                new JScrollPane(
                        mainPanel
                );

        scrollPane.setBorder(
                null
        );

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollPane
                .getVerticalScrollBar()
                .setUnitIncrement(
                        16
                );

        add(
                scrollPane,
                BorderLayout.CENTER
        );
    }

    public void setColourSaveAction(
            BiConsumer<Skill, Color> action
    )
    {
        colourSaveAction =
                action;
    }

    public void setColourLoadAction(
            Function<Skill, Color> action
    )
    {
        colourLoadAction =
                action;
    }

    public void setColourResetAction(
            Consumer<Skill> action
    )
    {
        colourResetAction =
                action;
    }

    public void setHeadingSaveAction(
            Consumer<Boolean> action
    )
    {
        headingSaveAction =
                action;
    }

    public void setUseIconHeading(
            boolean useIcon
    )
    {
        useIconHeading =
                useIcon;

        updateHeading();
    }

    public void refreshDefaultColours()
    {
        for (
                Map.Entry<Skill, SkillDisplay> entry
                : skillDisplays.entrySet()
        )
        {
            Skill skill =
                    entry.getKey();

            if (
                    Boolean.TRUE.equals(
                            customColours.get(
                                    skill
                            )
                    )
            )
            {
                continue;
            }

            Color defaultColour =
                    config.defaultProgressColour();

            skillColours.put(
                    skill,
                    defaultColour
            );

            entry
                    .getValue()
                    .progressBar
                    .setForeground(
                            defaultColour
                    );

            entry
                    .getValue()
                    .progressBar
                    .repaint();
        }
    }

    private void updateHeading()
    {
        if (useIconHeading)
        {
            titleLabel.setText(
                    null
            );

            titleLabel.setIcon(
                    titleIcon
            );

            titleLabel.setToolTipText(
                    "Prestige 92"
            );
        }
        else
        {
            titleLabel.setIcon(
                    null
            );

            titleLabel.setText(
                    "Prestige 92"
            );

            titleLabel.setForeground(
                    PRESTIGE_GOLD
            );

            titleLabel.setToolTipText(
                    null
            );
        }

        titleLabel.revalidate();
        titleLabel.repaint();
    }

    private void installHeadingMenu(
            java.awt.Component component
    )
    {
        component.addMouseListener(
                new MouseAdapter()
                {
                    @Override
                    public void mousePressed(
                            MouseEvent event
                    )
                    {
                        showHeadingMenuIfNeeded(
                                event
                        );
                    }

                    @Override
                    public void mouseReleased(
                            MouseEvent event
                    )
                    {
                        showHeadingMenuIfNeeded(
                                event
                        );
                    }
                }
        );
    }

    private void showHeadingMenuIfNeeded(
            MouseEvent event
    )
    {
        if (!event.isPopupTrigger())
        {
            return;
        }

        JPopupMenu menu =
                new JPopupMenu();

        JMenuItem headingItem =
                new JMenuItem(
                        useIconHeading
                                ? "Use Text Heading"
                                : "Use Plugin Icon"
                );

        headingItem.addActionListener(
                actionEvent ->
                {
                    useIconHeading =
                            !useIconHeading;

                    updateHeading();

                    if (
                            headingSaveAction != null
                    )
                    {
                        headingSaveAction.accept(
                                useIconHeading
                        );
                    }
                }
        );

        JMenuItem creatorItem =
                new JMenuItem(
                        "Who created this?"
                );

        creatorItem.addActionListener(
                actionEvent ->
                        showCreatorInformation()
        );

        menu.add(
                headingItem
        );

        menu.addSeparator();

        menu.add(
                creatorItem
        );

        menu.show(
                event.getComponent(),
                event.getX(),
                event.getY()
        );
    }

    private void showCreatorInformation()
    {
        JOptionPane.showMessageDialog(
                this,
                "Created by Bobcheese20",
                "Prestige 92",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void updateSkill(
            Skill skill,
            int prestigeXp,
            int prestigeLevel,
            int realXpRemaining
    )
    {
        SkillDisplay display =
                skillDisplays.get(
                        skill
                );

        if (
                display == null
        )
        {
            display =
                    createSkillDisplay(
                            skill
                    );

            skillDisplays.put(
                    skill,
                    display
            );

            skillsPanel.add(
                    display.panel
            );

            skillsPanel.add(
                    display.spacing
            );

            skillsPanel.revalidate();
            skillsPanel.repaint();
        }

        display.currentLevelLabel.setText(
                String.valueOf(
                        prestigeLevel
                )
        );

        /*
         * Prestige 92 is the end of the system and
         * corresponds to real level 99.
         */
        if (
                prestigeLevel >= 92
        )
        {
            display.skillLabel.setText(
                    skill.getName()
                            + "  ★ COMPLETE ★"
            );

            display.skillLabel.setForeground(
                    COMPLETION_GOLD
            );

            display.currentLevelLabel.setText(
                    "92"
            );

            display.currentLevelLabel.setForeground(
                    COMPLETION_GOLD
            );

            display.nextLevelLabel.setText(
                    "MAX"
            );

            display.nextLevelLabel.setForeground(
                    COMPLETION_GOLD
            );

            display.progressBar.setValue(
                    100
            );

            display.progressBar.setString(
                    "PRESTIGE 92"
            );

            display.progressBar.setForeground(
                    COMPLETION_GOLD
            );

            display.remainingLabel.setText(
                    "Level 99 reached"
            );

            display.remainingLabel.setForeground(
                    COMPLETION_GOLD
            );

            display.progressBar.setToolTipText(
                    "<html>"
                            + "<b>Prestige complete!</b><br>"
                            + "Prestige level: 92<br>"
                            + "Real level: 99"
                            + "</html>"
            );

            return;
        }

        /*
         * Restore ordinary colours/text if needed.
         */
        display.skillLabel.setText(
                skill.getName()
        );

        display.skillLabel.setForeground(
                Color.WHITE
        );

        display.currentLevelLabel.setForeground(
                Color.WHITE
        );

        display.nextLevelLabel.setForeground(
                Color.WHITE
        );

        display.remainingLabel.setForeground(
                Color.LIGHT_GRAY
        );

        display.progressBar.setForeground(
                getSkillColour(
                        skill
                )
        );

        int currentLevelXp =
                Experience.getXpForLevel(
                        prestigeLevel
                );

        int nextLevelXp =
                Experience.getXpForLevel(
                        prestigeLevel + 1
                );

        int xpIntoLevel =
                prestigeXp
                        - currentLevelXp;

        int xpNeededForLevel =
                nextLevelXp
                        - currentLevelXp;

        int prestigeXpRemaining =
                nextLevelXp
                        - prestigeXp;

        int percentage =
                xpNeededForLevel <= 0
                        ? 0
                        : (int) (
                        (long) xpIntoLevel
                                * 100
                                / xpNeededForLevel
                );

        percentage =
                Math.max(
                        0,
                        Math.min(
                                100,
                                percentage
                        )
                );

        display.nextLevelLabel.setText(
                String.valueOf(
                        prestigeLevel + 1
                )
        );

        display.progressBar.setValue(
                percentage
        );

        display.progressBar.setString(
                percentage + "%"
        );

        /*
         * IMPORTANT:
         * This is REAL RuneScape XP remaining,
         * not scaled virtual Prestige XP.
         */
        display.remainingLabel.setText(
                String.format(
                        "%,d real XP remaining",
                        Math.max(
                                0,
                                realXpRemaining
                        )
                )
        );

        display.progressBar.setToolTipText(
                "<html>"
                        + "<b>"
                        + skill.getName()
                        + " Prestige "
                        + prestigeLevel
                        + "</b><br>"
                        + "Prestige XP: "
                        + String.format(
                        "%,d",
                        prestigeXp
                )
                        + " / "
                        + String.format(
                        "%,d",
                        nextLevelXp
                )
                        + "<br>"
                        + "Prestige XP remaining: "
                        + String.format(
                        "%,d",
                        Math.max(
                                0,
                                prestigeXpRemaining
                        )
                )
                        + "<br>"
                        + "Actual XP to earn: "
                        + String.format(
                        "%,d",
                        Math.max(
                                0,
                                realXpRemaining
                        )
                )
                        + "</html>"
        );
    }

    public void removeSkill(
            Skill skill
    )
    {
        SkillDisplay display =
                skillDisplays.remove(
                        skill
                );

        if (
                display == null
        )
        {
            return;
        }

        skillsPanel.remove(
                display.panel
        );

        skillsPanel.remove(
                display.spacing
        );

        skillsPanel.revalidate();
        skillsPanel.repaint();
    }

    public void clearAllSkills()
    {
        skillDisplays.clear();

        skillsPanel.removeAll();

        skillsPanel.revalidate();
        skillsPanel.repaint();
    }

    private SkillDisplay createSkillDisplay(
            Skill skill
    )
    {
        JPanel panel =
                new JPanel();

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        panel.setBackground(
                ColorScheme.DARKER_GRAY_COLOR
        );

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                ColorScheme.MEDIUM_GRAY_COLOR
                        ),
                        new EmptyBorder(
                                8,
                                8,
                                8,
                                8
                        )
                )
        );

        panel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        85
                )
        );

        JLabel skillLabel =
                new JLabel(
                        skill.getName()
                );

        skillLabel.setAlignmentX(
                CENTER_ALIGNMENT
        );

        skillLabel.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        skillLabel.setFont(
                skillLabel
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                14f
                        )
        );

        skillLabel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        20
                )
        );

        JPanel progressRow =
                new JPanel(
                        new BorderLayout(
                                6,
                                0
                        )
                );

        progressRow.setBackground(
                ColorScheme.DARKER_GRAY_COLOR
        );

        progressRow.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        24
                )
        );

        JLabel currentLevelLabel =
                new JLabel(
                        "1",
                        SwingConstants.CENTER
                );

        currentLevelLabel.setFont(
                currentLevelLabel
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                14f
                        )
        );

        currentLevelLabel.setPreferredSize(
                new Dimension(
                        28,
                        22
                )
        );

        JLabel nextLevelLabel =
                new JLabel(
                        "2",
                        SwingConstants.CENTER
                );

        nextLevelLabel.setFont(
                nextLevelLabel
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                14f
                        )
        );

        nextLevelLabel.setPreferredSize(
                new Dimension(
                        28,
                        22
                )
        );

        JProgressBar progressBar =
                new JProgressBar(
                        0,
                        100
                );

        progressBar.setValue(
                0
        );

        progressBar.setStringPainted(
                true
        );

        progressBar.setString(
                "0%"
        );

        progressBar.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        22
                )
        );

        progressBar.setForeground(
                getSkillColour(
                        skill
                )
        );

        progressRow.add(
                currentLevelLabel,
                BorderLayout.WEST
        );

        progressRow.add(
                progressBar,
                BorderLayout.CENTER
        );

        progressRow.add(
                nextLevelLabel,
                BorderLayout.EAST
        );

        JLabel remainingLabel =
                new JLabel(
                        "0 real XP remaining"
                );

        remainingLabel.setAlignmentX(
                CENTER_ALIGNMENT
        );

        remainingLabel.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        remainingLabel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        20
                )
        );

        remainingLabel.setFont(
                remainingLabel
                        .getFont()
                        .deriveFont(
                                Font.PLAIN,
                                11f
                        )
        );

        panel.add(
                skillLabel
        );

        panel.add(
                Box.createVerticalStrut(
                        5
                )
        );

        panel.add(
                progressRow
        );

        panel.add(
                Box.createVerticalStrut(
                        4
                )
        );

        panel.add(
                remainingLabel
        );

        installSkillMenu(
                panel,
                skill
        );

        installSkillMenu(
                skillLabel,
                skill
        );

        installSkillMenu(
                progressRow,
                skill
        );

        installSkillMenu(
                currentLevelLabel,
                skill
        );

        installSkillMenu(
                progressBar,
                skill
        );

        installSkillMenu(
                nextLevelLabel,
                skill
        );

        installSkillMenu(
                remainingLabel,
                skill
        );

        java.awt.Component spacing =
                Box.createVerticalStrut(
                        10
                );

        return new SkillDisplay(
                panel,
                spacing,
                skillLabel,
                currentLevelLabel,
                nextLevelLabel,
                progressBar,
                remainingLabel
        );
    }

    private Color getSkillColour(
            Skill skill
    )
    {
        Color cachedColour =
                skillColours.get(
                        skill
                );

        if (
                cachedColour != null
        )
        {
            return cachedColour;
        }

        Color loadedColour =
                null;

        if (
                colourLoadAction != null
        )
        {
            loadedColour =
                    colourLoadAction.apply(
                            skill
                    );
        }

        if (
                loadedColour != null
        )
        {
            skillColours.put(
                    skill,
                    loadedColour
            );

            customColours.put(
                    skill,
                    true
            );

            return loadedColour;
        }

        Color defaultColour =
                config.defaultProgressColour();

        skillColours.put(
                skill,
                defaultColour
        );

        customColours.put(
                skill,
                false
        );

        return defaultColour;
    }

    private void chooseSkillColour(
            Skill skill
    )
    {
        Color currentColour =
                getSkillColour(
                        skill
                );

        Color chosenColour =
                JColorChooser.showDialog(
                        this,
                        "Choose "
                                + skill.getName()
                                + " Colour",
                        currentColour
                );

        if (
                chosenColour == null
        )
        {
            return;
        }

        skillColours.put(
                skill,
                chosenColour
        );

        customColours.put(
                skill,
                true
        );

        SkillDisplay display =
                skillDisplays.get(
                        skill
                );

        if (
                display != null
        )
        {
            display.progressBar.setForeground(
                    chosenColour
            );

            display.progressBar.repaint();
        }

        if (
                colourSaveAction != null
        )
        {
            colourSaveAction.accept(
                    skill,
                    chosenColour
            );
        }
    }

    private void resetSkillColour(
            Skill skill
    )
    {
        Color defaultColour =
                config.defaultProgressColour();

        skillColours.put(
                skill,
                defaultColour
        );

        customColours.put(
                skill,
                false
        );

        SkillDisplay display =
                skillDisplays.get(
                        skill
                );

        if (
                display != null
        )
        {
            display.progressBar.setForeground(
                    defaultColour
            );

            display.progressBar.repaint();
        }

        if (
                colourResetAction != null
        )
        {
            colourResetAction.accept(
                    skill
            );
        }
    }

    private void installSkillMenu(
            java.awt.Component component,
            Skill skill
    )
    {
        component.addMouseListener(
                new MouseAdapter()
                {
                    @Override
                    public void mousePressed(
                            MouseEvent event
                    )
                    {
                        showSkillMenuIfNeeded(
                                event,
                                skill
                        );
                    }

                    @Override
                    public void mouseReleased(
                            MouseEvent event
                    )
                    {
                        showSkillMenuIfNeeded(
                                event,
                                skill
                        );
                    }
                }
        );
    }

    private void showSkillMenuIfNeeded(
            MouseEvent event,
            Skill skill
    )
    {
        if (
                !event.isPopupTrigger()
        )
        {
            return;
        }

        JPopupMenu menu =
                new JPopupMenu();

        JMenuItem colourItem =
                new JMenuItem(
                        "Change Colour..."
                );

        colourItem.addActionListener(
                actionEvent ->
                        chooseSkillColour(
                                skill
                        )
        );

        JMenuItem resetColourItem =
                new JMenuItem(
                        "Reset Colour"
                );

        resetColourItem.addActionListener(
                actionEvent ->
                        resetSkillColour(
                                skill
                        )
        );

        JMenuItem removeItem =
                new JMenuItem(
                        "Remove "
                                + skill.getName()
                );

        removeItem.addActionListener(
                actionEvent ->
                        removeSkill(
                                skill
                        )
        );

        JMenuItem clearAllItem =
                new JMenuItem(
                        "Clear All"
                );

        clearAllItem.addActionListener(
                actionEvent ->
                        clearAllSkills()
        );

        menu.add(
                colourItem
        );

        menu.add(
                resetColourItem
        );

        menu.addSeparator();

        menu.add(
                removeItem
        );

        menu.add(
                clearAllItem
        );

        menu.show(
                event.getComponent(),
                event.getX(),
                event.getY()
        );
    }

    private void installClearAllMenu(
            java.awt.Component component
    )
    {
        component.addMouseListener(
                new MouseAdapter()
                {
                    @Override
                    public void mousePressed(
                            MouseEvent event
                    )
                    {
                        showClearAllMenuIfNeeded(
                                event
                        );
                    }

                    @Override
                    public void mouseReleased(
                            MouseEvent event
                    )
                    {
                        showClearAllMenuIfNeeded(
                                event
                        );
                    }
                }
        );
    }

    private void showClearAllMenuIfNeeded(
            MouseEvent event
    )
    {
        if (
                !event.isPopupTrigger()
        )
        {
            return;
        }

        JPopupMenu menu =
                new JPopupMenu();

        JMenuItem clearAllItem =
                new JMenuItem(
                        "Clear All"
                );

        clearAllItem.addActionListener(
                actionEvent ->
                        clearAllSkills()
        );

        menu.add(
                clearAllItem
        );

        menu.show(
                event.getComponent(),
                event.getX(),
                event.getY()
        );
    }

    private static class SkillDisplay
    {
        private final JPanel panel;

        private final java.awt.Component spacing;

        private final JLabel skillLabel;

        private final JLabel currentLevelLabel;

        private final JLabel nextLevelLabel;

        private final JProgressBar progressBar;

        private final JLabel remainingLabel;

        private SkillDisplay(
                JPanel panel,
                java.awt.Component spacing,
                JLabel skillLabel,
                JLabel currentLevelLabel,
                JLabel nextLevelLabel,
                JProgressBar progressBar,
                JLabel remainingLabel
        )
        {
            this.panel =
                    panel;

            this.spacing =
                    spacing;

            this.skillLabel =
                    skillLabel;

            this.currentLevelLabel =
                    currentLevelLabel;

            this.nextLevelLabel =
                    nextLevelLabel;

            this.progressBar =
                    progressBar;

            this.remainingLabel =
                    remainingLabel;
        }
    }
}