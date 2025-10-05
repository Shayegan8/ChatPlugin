package shayegan8.github.gui;

import java.util.List;
import java.util.Map;

public sealed abstract class Gui permits IMenu, IMute, IInvite, IRequest, IDelete {

	public abstract int getSize();

	public String getName() {
		return null;
	}

	public abstract String getTitle();

	public List<String> getLore() {
		return null;
	}

	public abstract Map<String, Entry> getEntries();
}
