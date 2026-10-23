package com.flix3n;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.inject.Provides;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Polygon;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.PluginPanel;

@PluginDescriptor(name = "Simple Counter")
public class CounterPlugin extends Plugin
{
	private static final Color ACCENT_COLOR = new Color(66, 105, 132);
	private final CounterPanel panel = new CounterPanel();

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private CounterConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Gson gson;

	private NavigationButton navigationButton;

	@Provides
	CounterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CounterConfig.class);
	}

	@Override
	protected void startUp()
	{
		panel.load(config.counters(), config.selectedCounter());
		navigationButton = NavigationButton.builder()
			.tooltip("Simple Counter")
			.icon(createIcon())
			.priority(5)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navigationButton);
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navigationButton);
		navigationButton = null;
	}

	@Override
	public void resetConfiguration()
	{
		panel.resetAllCounters();
	}

	private BufferedImage createIcon()
	{
		BufferedImage icon = new BufferedImage(24, 18, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = icon.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(ColorScheme.LIGHT_GRAY_COLOR);
		graphics.fillRoundRect(0, 1, 24, 16, 5, 5);
		graphics.setColor(ColorScheme.DARKER_GRAY_COLOR);
		graphics.fillRoundRect(2, 3, 20, 12, 3, 3);
		graphics.setColor(ColorScheme.TEXT_COLOR);
		graphics.setFont(new Font(Font.MONOSPACED, Font.BOLD, 8));
		graphics.drawString("012", 3, 12);
		graphics.dispose();
		return icon;
	}

	private static class RoundedButton extends JButton
	{
		private final boolean primary;

		private RoundedButton(String text, boolean primary)
		{
			super(text);
			this.primary = primary;
			setForeground(primary ? Color.WHITE : ColorScheme.TEXT_COLOR);
			setFont(getFont().deriveFont(Font.BOLD, 13f));
			setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
			setOpaque(false);
			setRolloverEnabled(true);
			setPreferredSize(new java.awt.Dimension(52, 32));
		}

		@Override
		protected void paintComponent(Graphics graphics)
		{
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Color fill = primary ? ACCENT_COLOR : ColorScheme.MEDIUM_GRAY_COLOR;
			if (getModel().isPressed())
			{
				fill = shade(fill, 0.82f);
			}
			else if (getModel().isRollover())
			{
				fill = shade(fill, 1.12f);
			}

			g.setColor(fill);
			g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
			g.setColor(ColorScheme.BORDER_COLOR);
			g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
			g.dispose();
			super.paintComponent(graphics);
		}

		private Color shade(Color color, float multiplier)
		{
			return new Color(
				Math.min(255, Math.round(color.getRed() * multiplier)),
				Math.min(255, Math.round(color.getGreen() * multiplier)),
				Math.min(255, Math.round(color.getBlue() * multiplier)));
		}
	}

	private static class CalculatorDisclosureButton extends RoundedButton
	{
		private boolean expanded;

		private CalculatorDisclosureButton()
		{
			super("Trip Calculator", false);
			setHorizontalAlignment(SwingConstants.CENTER);
		}

		@Override
		protected void paintComponent(Graphics graphics)
		{
			super.paintComponent(graphics);
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int centerX = getWidth() - 16;
			int centerY = getHeight() / 2;
			g.setColor(ColorScheme.TEXT_COLOR);
			if (expanded)
			{
				g.fillPolygon(new Polygon(
					new int[]{centerX - 4, centerX + 4, centerX},
					new int[]{centerY + 2, centerY + 2, centerY - 3}, 3));
			}
			else
			{
				g.fillPolygon(new Polygon(
					new int[]{centerX - 4, centerX + 4, centerX},
					new int[]{centerY - 2, centerY - 2, centerY + 3}, 3));
			}
			g.dispose();
		}
	}

	private static class CounterDropdownButton extends RoundedButton
	{
		private boolean expanded;

		private CounterDropdownButton()
		{
			super("Counter", false);
			setHorizontalAlignment(SwingConstants.LEFT);
			setForeground(ColorScheme.TEXT_COLOR);
			setFont(getFont().deriveFont(Font.PLAIN, 13f));
			setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 26));
			setPreferredSize(new java.awt.Dimension(220, 32));
		}

		@Override
		protected void paintComponent(Graphics graphics)
		{
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Color fill = ColorScheme.DARK_GRAY_COLOR;
			if (getModel().isPressed())
			{
				fill = ColorScheme.MEDIUM_GRAY_COLOR;
			}
			else if (getModel().isRollover())
			{
				fill = ColorScheme.DARK_GRAY_COLOR.brighter();
			}
			g.setColor(fill);
			g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
			g.setColor(ColorScheme.BORDER_COLOR);
			g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
			g.setFont(getFont());
			g.setColor(getForeground());
			FontMetrics metrics = g.getFontMetrics();
			String text = getText() == null ? "" : getText();
			int availableWidth = Math.max(0, getWidth() - 40);
			if (metrics.stringWidth(text) > availableWidth)
			{
				String shortened = text;
				while (!shortened.isEmpty() && metrics.stringWidth(shortened + "...") > availableWidth)
				{
					int lastCodePoint = shortened.codePointBefore(shortened.length());
					shortened = shortened.substring(0, shortened.length() - Character.charCount(lastCodePoint));
				}
				text = shortened.isEmpty() ? "" : shortened + "...";
			}
			int textX = 8 + Math.max(0, (availableWidth - metrics.stringWidth(text)) / 2);
			int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
			g.drawString(text, textX, textY);
			int centerX = getWidth() - 14;
			int centerY = getHeight() / 2;
			g.setColor(ColorScheme.TEXT_COLOR);
			g.fillPolygon(new Polygon(
				new int[]{centerX - 4, centerX + 4, centerX},
				expanded
					? new int[]{centerY + 2, centerY + 2, centerY - 3}
					: new int[]{centerY - 2, centerY - 2, centerY + 3}, 3));
			g.dispose();
		}
	}

	private static class RemoveCounterButton extends JButton
	{
		private RemoveCounterButton(String counterName)
		{
			getAccessibleContext().setAccessibleName("Remove " + counterName);
			setPreferredSize(new java.awt.Dimension(26, 24));
			setBorder(BorderFactory.createEmptyBorder());
			setContentAreaFilled(false);
			setFocusPainted(false);
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics graphics)
		{
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if (getModel().isRollover() || getModel().isPressed())
			{
				g.setColor(ColorScheme.MEDIUM_GRAY_COLOR);
				g.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
			}
			g.setColor(new Color(220, 90, 90));
			g.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
			int centerX = getWidth() / 2;
			int centerY = getHeight() / 2;
			g.drawLine(centerX - 4, centerY - 4, centerX + 4, centerY + 4);
			g.drawLine(centerX + 4, centerY - 4, centerX - 4, centerY + 4);
			g.dispose();
		}
	}

	private void saveCounters()
	{
		configManager.setConfiguration(CounterConfig.GROUP, "counters", gson.toJson(panel.counters));
		configManager.setConfiguration(CounterConfig.GROUP, "selectedCounter", panel.selectedCounter);
	}

	private class CounterPanel extends PluginPanel
	{
		private final Map<String, Integer> counters = new LinkedHashMap<>();
		private final CounterDropdownButton counterSelector = new CounterDropdownButton();
		private JPanel counterMenuPanel;
		private final JLabel value = new JLabel("0", SwingConstants.CENTER);
		private final JTextField numberInput = new JTextField();
		private final JLabel inputMessage = new JLabel(" ", SwingConstants.CENTER);
		private final JTextField slotsInput = new JTextField("0");
		private final JTextField goalInput = new JTextField("0");
		private final JLabel calculatorMessage = new JLabel(" ", SwingConstants.CENTER);
		private final CalculatorDisclosureButton calculatorToggle = new CalculatorDisclosureButton();
		private final JPanel calculatorView = createCalculatorView();
		private String selectedCounter = "Counter";

		private CounterPanel()
		{
			setLayout(new BorderLayout());
			setBackground(ColorScheme.DARKER_GRAY_COLOR);
			setOpaque(true);
			JPanel content = new JPanel(new BorderLayout(0, 10));
			content.setOpaque(false);
			content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			content.add(createCounterView(), BorderLayout.CENTER);
			content.add(createCalculatorDisclosure(), BorderLayout.SOUTH);
			add(content, BorderLayout.CENTER);
		}

		private JPanel createCalculatorDisclosure()
		{
			JPanel disclosure = new JPanel(new BorderLayout(0, 6));
			disclosure.setOpaque(false);
			calculatorToggle.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 32));
			calculatorView.setVisible(false);
			calculatorToggle.addActionListener(event -> {
				boolean expanded = !calculatorView.isVisible();
				calculatorView.setVisible(expanded);
				calculatorToggle.expanded = expanded;
				disclosure.revalidate();
				disclosure.repaint();
			});
			disclosure.add(calculatorToggle, BorderLayout.NORTH);
			disclosure.add(calculatorView, BorderLayout.CENTER);
			return disclosure;
		}

		private JPanel createCounterView()
		{
			JPanel view = new JPanel(new BorderLayout(0, 14));
			view.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
			view.setOpaque(false);

			value.setForeground(ColorScheme.TEXT_COLOR);
			value.setFont(value.getFont().deriveFont(Font.BOLD, 40f));

			JPanel header = new JPanel(new BorderLayout(0, 4));
			header.setOpaque(false);

			counterSelector.addActionListener(event -> toggleCounterMenu());
			counterSelector.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 32));
			header.add(counterSelector, BorderLayout.NORTH);
			counterMenuPanel = new JPanel();
			counterMenuPanel.setLayout(new javax.swing.BoxLayout(counterMenuPanel, javax.swing.BoxLayout.Y_AXIS));
			counterMenuPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			counterMenuPanel.setBorder(new LineBorder(ColorScheme.BORDER_COLOR));
			counterMenuPanel.setOpaque(true);
			counterMenuPanel.setVisible(false);
			header.add(counterMenuPanel, BorderLayout.CENTER);

			JPanel countCard = new JPanel(new BorderLayout());
			countCard.setBackground(ColorScheme.DARK_GRAY_COLOR);
			countCard.setBorder(BorderFactory.createCompoundBorder(
				new LineBorder(ColorScheme.BORDER_COLOR),
				BorderFactory.createEmptyBorder(16, 8, 16, 8)));
			countCard.add(value, BorderLayout.CENTER);

			JPanel controls = new JPanel(new GridLayout(1, 2, 8, 0));
			controls.setOpaque(false);
			JButton decrement = new RoundedButton("-1", false);
			JButton increment = new RoundedButton("+1", true);
			decrement.addActionListener(event -> updateCount(-1));
			increment.addActionListener(event -> updateCount(1));
			controls.add(decrement);
			controls.add(increment);

			JLabel setLabel = new JLabel("Set count", SwingConstants.CENTER);
			setLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			setLabel.setFont(setLabel.getFont().deriveFont(Font.PLAIN, 12f));
			JPanel setControls = new JPanel(new BorderLayout(8, 0));
			setControls.setOpaque(false);
			numberInput.setToolTipText("Enter a non-negative whole number");
			numberInput.setBackground(ColorScheme.DARK_GRAY_COLOR);
			numberInput.setForeground(ColorScheme.TEXT_COLOR);
			numberInput.setCaretColor(ColorScheme.TEXT_COLOR);
			numberInput.setBorder(BorderFactory.createCompoundBorder(
				new LineBorder(ColorScheme.BORDER_COLOR),
				BorderFactory.createEmptyBorder(0, 6, 0, 6)));
			numberInput.setPreferredSize(new java.awt.Dimension(70, 30));
			JButton setCount = new RoundedButton("Set", false);
			setCount.addActionListener(event -> setCountFromInput());
			numberInput.addActionListener(event -> setCountFromInput());
			setControls.add(numberInput, BorderLayout.CENTER);
			setControls.add(setCount, BorderLayout.EAST);

			JButton reset = new RoundedButton("Reset", false);
			reset.setPreferredSize(new java.awt.Dimension(132, 32));
			reset.addActionListener(event -> setSelectedCount(0));
			JPanel resetWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			resetWrapper.setOpaque(false);
			resetWrapper.add(reset);
			inputMessage.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			inputMessage.setFont(inputMessage.getFont().deriveFont(Font.PLAIN, 11f));
			inputMessage.setHorizontalAlignment(SwingConstants.CENTER);
			inputMessage.setPreferredSize(new java.awt.Dimension(0, 18));
			inputMessage.setMinimumSize(new java.awt.Dimension(0, 18));
			inputMessage.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 18));

			JPanel footer = new JPanel();
			footer.setOpaque(false);
			footer.setLayout(new java.awt.GridBagLayout());
			java.awt.GridBagConstraints row = new java.awt.GridBagConstraints();
			row.gridx = 0;
			row.weightx = 1;
			row.fill = java.awt.GridBagConstraints.HORIZONTAL;
			row.gridy = 0;
			footer.add(controls, row);
			row.gridy++;
			row.insets = new java.awt.Insets(12, 0, 0, 0);
			footer.add(setLabel, row);
			row.gridy++;
			row.insets = new java.awt.Insets(4, 0, 0, 0);
			footer.add(setControls, row);
			row.gridy++;
			row.insets = new java.awt.Insets(8, 0, 0, 0);
			footer.add(inputMessage, row);
			row.gridy++;
			row.insets = new java.awt.Insets(4, 0, 0, 0);
			footer.add(resetWrapper, row);

			view.add(header, BorderLayout.NORTH);
			view.add(countCard, BorderLayout.CENTER);
			view.add(footer, BorderLayout.SOUTH);
			return view;
		}

		private void toggleCounterMenu()
		{
			setCounterMenuOpen(!counterMenuPanel.isVisible());
		}

		private void setCounterMenuOpen(boolean open)
		{
			counterMenuPanel.setVisible(open);
			counterSelector.expanded = open;
			counterMenuPanel.getParent().revalidate();
			counterMenuPanel.getParent().repaint();
		}

		private void rebuildCounterMenu()
		{
			if (counterMenuPanel == null)
			{
				return;
			}
			counterMenuPanel.removeAll();
			for (String name : counters.keySet())
			{
				JPanel row = new JPanel(new BorderLayout(4, 0));
				row.setOpaque(true);
				row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
				row.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
				row.setPreferredSize(new java.awt.Dimension(counterSelector.getPreferredSize().width - 2, 30));
				row.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 30));
				row.setAlignmentX(Component.LEFT_ALIGNMENT);

				JButton selectCounter = new JButton(name);
				selectCounter.setHorizontalAlignment(SwingConstants.LEFT);
				selectCounter.setForeground(ColorScheme.TEXT_COLOR);
				selectCounter.setFont(selectCounter.getFont().deriveFont(Font.PLAIN, 12f));
				selectCounter.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
				selectCounter.setContentAreaFilled(false);
				selectCounter.setFocusPainted(false);
				selectCounter.setOpaque(false);
				selectCounter.addActionListener(event -> {
					selectedCounter = name;
					counterSelector.setText(name);
					showSelectedCount();
					saveCounters();
					setCounterMenuOpen(false);
				});
				selectCounter.addMouseListener(new java.awt.event.MouseAdapter()
				{
					@Override
					public void mouseEntered(java.awt.event.MouseEvent event)
					{
						row.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
					}

					@Override
					public void mouseExited(java.awt.event.MouseEvent event)
					{
						row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
					}
				});
				RemoveCounterButton remove = new RemoveCounterButton(name);
				remove.addActionListener(event -> {
					setCounterMenuOpen(false);
					removeCounter(name);
				});
				row.add(selectCounter, BorderLayout.CENTER);
				row.add(remove, BorderLayout.EAST);
				counterMenuPanel.add(row);
			}

			JButton addCounterItem = new JButton("+ Add counter...");
			addCounterItem.setHorizontalAlignment(SwingConstants.LEFT);
			addCounterItem.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			addCounterItem.setFont(addCounterItem.getFont().deriveFont(Font.PLAIN, 12f));
			addCounterItem.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
			addCounterItem.setContentAreaFilled(false);
			addCounterItem.setFocusPainted(false);
			addCounterItem.setOpaque(false);
			addCounterItem.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 30));
			addCounterItem.setAlignmentX(Component.LEFT_ALIGNMENT);
			addCounterItem.addActionListener(event -> {
				setCounterMenuOpen(false);
				addCounter();
			});
			counterMenuPanel.add(addCounterItem);
			counterMenuPanel.revalidate();
			counterMenuPanel.repaint();
		}

		private JPanel createCalculatorView()
		{
			JPanel view = new JPanel(new BorderLayout(0, 14));
			view.setOpaque(false);
			view.setBorder(BorderFactory.createCompoundBorder(
				new LineBorder(ColorScheme.BORDER_COLOR),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));

			styleInput(slotsInput);
			styleInput(goalInput);
			JPanel slotsField = createInputField("Amount per trip", slotsInput);
			JPanel goalField = createInputField("Total goal", goalInput);
			JPanel fields = new JPanel();
			fields.setOpaque(false);
			fields.setLayout(new javax.swing.BoxLayout(fields, javax.swing.BoxLayout.Y_AXIS));
			fields.add(slotsField);
			fields.add(javax.swing.Box.createVerticalStrut(12));
			fields.add(goalField);

			JButton calculate = new RoundedButton("Calculate trips", true);
			calculate.setPreferredSize(new java.awt.Dimension(150, 32));
			calculate.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 34));
			calculate.addActionListener(event -> calculateTrips());
			slotsInput.addActionListener(event -> calculateTrips());
			goalInput.addActionListener(event -> calculateTrips());

			calculatorMessage.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			calculatorMessage.setFont(calculatorMessage.getFont().deriveFont(Font.PLAIN, 11f));
			JPanel footer = new JPanel();
			footer.setOpaque(false);
			footer.setLayout(new javax.swing.BoxLayout(footer, javax.swing.BoxLayout.Y_AXIS));
			footer.add(calculate);
			footer.add(javax.swing.Box.createVerticalStrut(8));
			footer.add(calculatorMessage);
			view.add(fields, BorderLayout.CENTER);
			view.add(footer, BorderLayout.SOUTH);
			return view;
		}

		private JPanel createInputField(String labelText, JTextField field)
		{
			JPanel panel = new JPanel(new BorderLayout(0, 4));
			panel.setOpaque(false);
			JLabel label = new JLabel(labelText);
			label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			label.setFont(label.getFont().deriveFont(Font.PLAIN, 12f));
			panel.add(label, BorderLayout.NORTH);
			panel.add(field, BorderLayout.CENTER);
			panel.setPreferredSize(new java.awt.Dimension(180, 52));
			panel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 52));
			panel.setAlignmentX(Component.LEFT_ALIGNMENT);
			return panel;
		}

		private void styleInput(JTextField field)
		{
			field.setBackground(ColorScheme.DARK_GRAY_COLOR);
			field.setForeground(ColorScheme.TEXT_COLOR);
			field.setCaretColor(ColorScheme.TEXT_COLOR);
			field.setBorder(BorderFactory.createCompoundBorder(
				new LineBorder(ColorScheme.BORDER_COLOR),
				BorderFactory.createEmptyBorder(0, 6, 0, 6)));
			field.setPreferredSize(new java.awt.Dimension(90, 32));
		}

		private void calculateTrips()
		{
			try
			{
				long amountPerTrip = Long.parseLong(slotsInput.getText().trim());
				long goal = Long.parseLong(goalInput.getText().trim());
				if (amountPerTrip <= 0)
				{
					throw new IllegalArgumentException("Amount per trip must be greater than 0.");
				}
				if (goal < 0)
				{
					throw new IllegalArgumentException("Total goal cannot be negative.");
				}

				long trips = goal / amountPerTrip + (goal % amountPerTrip == 0 ? 0 : 1);
				if (trips > Integer.MAX_VALUE)
				{
					throw new IllegalArgumentException("Trip count is too large for the counter.");
				}

				setSelectedCount((int) trips);
				calculatorMessage.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
				if (goal % amountPerTrip == 0)
				{
					calculatorMessage.setText("Counter set to " + trips + " trips.");
				}
				else
				{
					BigDecimal roundedUpBy = BigDecimal.valueOf(amountPerTrip - goal % amountPerTrip)
						.divide(BigDecimal.valueOf(amountPerTrip), 1, RoundingMode.HALF_UP);
					String amount = roundedUpBy.signum() == 0
						? "<0.1"
						: roundedUpBy.stripTrailingZeros().toPlainString();
					calculatorMessage.setText("<html><center>Counter set to " + trips
						+ " trips.<br>Rounded up by " + amount + "</center></html>");
				}
			}
			catch (NumberFormatException ex)
			{
				calculatorMessage.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
				calculatorMessage.setText("Enter whole numbers for both values.");
			}
			catch (IllegalArgumentException ex)
			{
				calculatorMessage.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
				calculatorMessage.setText(ex.getMessage());
			}
		}

		private void load(String savedCounters, String savedSelection)
		{
			counters.clear();
			if (savedCounters != null && !savedCounters.isEmpty())
			{
				try
				{
					JsonObject loaded = gson.fromJson(savedCounters, JsonObject.class);
					if (loaded != null)
					{
						for (Map.Entry<String, JsonElement> entry : loaded.entrySet())
						{
							JsonElement element = entry.getValue();
							if (entry.getKey() != null && !entry.getKey().trim().isEmpty()
								&& element != null && element.isJsonPrimitive()
								&& element.getAsJsonPrimitive().isNumber())
							{
								try
								{
									int count = Integer.parseInt(element.getAsString());
									counters.put(entry.getKey(), Math.max(0, count));
								}
								catch (NumberFormatException ex)
								{
									// Ignore an invalid value without discarding other saved counters.
								}
							}
						}
					}
				}
				catch (JsonParseException ex)
				{
					counters.clear();
				}
			}

			if (counters.containsKey("General") && !counters.containsKey("Counter"))
			{
				Map<String, Integer> renamedCounters = new LinkedHashMap<>();
				for (Map.Entry<String, Integer> entry : counters.entrySet())
				{
					renameCounterEntry(renamedCounters, entry);
				}
				counters.clear();
				counters.putAll(renamedCounters);
				if ("General".equals(savedSelection))
				{
					savedSelection = "Counter";
				}
			}

			if (counters.isEmpty())
			{
				counters.put("Counter", 0);
			}
			selectedCounter = counters.containsKey(savedSelection) ? savedSelection : counters.keySet().iterator().next();
			refreshCounterSelector();
			showSelectedCount();
			saveCounters();
		}

		private void resetAllCounters()
		{
			setCounterMenuOpen(false);
			counters.clear();
			counters.put("Counter", 0);
			selectedCounter = "Counter";
			numberInput.setText("");
			slotsInput.setText("0");
			goalInput.setText("0");
			inputMessage.setText(" ");
			calculatorMessage.setText(" ");
			refreshCounterSelector();
			showSelectedCount();
			saveCounters();
		}

		private void renameCounterEntry(Map<String, Integer> target, Map.Entry<String, Integer> entry)
		{
			String name = "General".equals(entry.getKey()) ? "Counter" : entry.getKey();
			target.put(name, entry.getValue());
		}

		private void updateCount(int delta)
		{
			int count = counters.get(selectedCounter);
			if (delta < 0)
			{
				count = Math.max(0, count - 1);
			}
			else if (count < Integer.MAX_VALUE)
			{
				count++;
			}
			setSelectedCount(count);
		}

		private void setCountFromInput()
		{
			try
			{
				int newCount = Integer.parseInt(numberInput.getText().trim());
				if (newCount < 0)
				{
					throw new NumberFormatException();
				}

				setSelectedCount(newCount);
				inputMessage.setText(" ");
			}
			catch (NumberFormatException ex)
			{
				inputMessage.setText("Enter a whole number");
			}
		}

		private void setSelectedCount(int count)
		{
			counters.put(selectedCounter, count);
			showSelectedCount();
			saveCounters();
		}

		private void showSelectedCount()
		{
			value.setText(Integer.toString(counters.getOrDefault(selectedCounter, 0)));
		}

		private void addCounter()
		{
			JOptionPane prompt = new JOptionPane("Name for the new counter:",
				JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
			prompt.setWantsInput(true);
			prompt.setInitialSelectionValue("");
			Object result = showShiftedDialog(prompt, "Add Counter");
			if (!Integer.valueOf(JOptionPane.OK_OPTION).equals(result))
			{
				return;
			}
			Object input = prompt.getInputValue();
			String name = input == null ? "" : input.toString();
			name = name.trim();
			if (name.isEmpty() || containsCounter(name))
			{
				inputMessage.setText(name.isEmpty() ? "Enter a counter name" : "That counter already exists");
				return;
			}

			counters.put(name, 0);
			selectedCounter = name;
			refreshCounterSelector();
			showSelectedCount();
			inputMessage.setText(" ");
			saveCounters();
		}

		private boolean containsCounter(String name)
		{
			for (String existing : counters.keySet())
			{
				if (existing.equalsIgnoreCase(name))
				{
					return true;
				}
			}
			return false;
		}

		private void removeCounter(String counterName)
		{
			if (counters.size() == 1)
			{
				inputMessage.setText("Keep at least one counter");
				return;
			}

			JOptionPane prompt = new JOptionPane("Delete counter \"" + counterName + "\"?",
				JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
			Object result = showShiftedDialog(prompt, "Remove Counter");
			if (!Integer.valueOf(JOptionPane.YES_OPTION).equals(result))
			{
				return;
			}

			counters.remove(counterName);
			if (selectedCounter.equals(counterName))
			{
				selectedCounter = counters.keySet().iterator().next();
			}
			refreshCounterSelector();
			showSelectedCount();
			inputMessage.setText(" ");
			saveCounters();
		}

		private Object showShiftedDialog(JOptionPane optionPane, String title)
		{
			JDialog dialog = optionPane.createDialog(this, title);
			Point panelLocation = getLocationOnScreen();
			Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
			int desiredX = panelLocation.x - dialog.getWidth() - getWidth() - 12;
			int desiredY = panelLocation.y + 46;
			int x = Math.max(bounds.x, Math.min(desiredX, bounds.x + bounds.width - dialog.getWidth()));
			int y = Math.max(bounds.y, Math.min(desiredY, bounds.y + bounds.height - dialog.getHeight()));
			dialog.setLocation(x, y);
			dialog.setVisible(true);
			dialog.dispose();
			return optionPane.getValue();
		}

		private void refreshCounterSelector()
		{
			counterSelector.setText(selectedCounter);
			rebuildCounterMenu();
		}
	}
}
