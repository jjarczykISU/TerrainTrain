package UI;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;

import javax.swing.JPanel;

public class OverlayPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private Image background = null;
	private Image overlay = null;
	
	public OverlayPanel(LayoutManager manager) {
		super(manager);
	}
	
	public void setBackgroundImage(Image background) {
		this.background = background;
	}
	
	public void setOverlayImage(Image overlay) {
		this.overlay = overlay;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (background != null)
			g.drawImage(background, 0, 0, this);
		if (overlay != null)
			g.drawImage(overlay, 0, 0, this);
	}
	
}
