package org.aio.gui.styled_components;

import org.aio.gui.utils.ColourScheme;

import javax.swing.*;
import java.awt.*;

public class StyledJPanel extends JPanel {

    public StyledJPanel(final LayoutManager layoutManager) {
        super(layoutManager);
        setBackground(ColourScheme.PANEL_BACKGROUND_GREY);
    }

    public StyledJPanel() {
        setBackground(ColourScheme.PANEL_BACKGROUND_GREY);
    }
}
