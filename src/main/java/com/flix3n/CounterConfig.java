package com.flix3n;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(CounterConfig.GROUP)
public interface CounterConfig extends Config
{
	String GROUP = "simple-counter";

	@ConfigItem(
		keyName = "counters",
		name = "Saved counters",
		description = "The names and values of your counters",
		hidden = true
	)
	default String counters()
	{
		return "";
	}

	@ConfigItem(
		keyName = "selectedCounter",
		name = "Selected counter",
		description = "The counter selected when the plugin was last used",
		hidden = true
	)
	default String selectedCounter()
	{
		return "Counter";
	}
}
