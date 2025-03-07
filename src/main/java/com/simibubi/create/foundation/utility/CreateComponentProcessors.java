package com.simibubi.create.foundation.utility;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;

import net.createmod.catnip.nbt.NBTProcessors;
import net.minecraft.core.component.DataComponentPatch;

public class CreateComponentProcessors {
	@SuppressWarnings({"unchecked", "OptionalAssignedToNull"})
	public static DataComponentPatch clipboardProcessor(DataComponentPatch data) {
		data.forget(type -> {
			if (type.equals(AllDataComponents.CLIPBOARD_PAGES)) {
				Optional<List<List<ClipboardEntry>>> optional = (Optional<List<List<ClipboardEntry>>>) data.get(type);
				if (optional != null) {
					for (List<ClipboardEntry> page : optional.orElse(List.of())) {
						for (ClipboardEntry entry : page) {
							if (NBTProcessors.textComponentHasClickEvent(entry.text))
								return true;
						}
					}
				}
			}

			return false;
		});

		return data;
	}
}
