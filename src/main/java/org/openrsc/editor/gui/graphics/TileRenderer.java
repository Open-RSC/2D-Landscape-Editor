package org.openrsc.editor.gui.graphics;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.openrsc.editor.Util;
import org.openrsc.editor.event.DisplayConfigurationUpdateEvent;
import org.openrsc.editor.event.EventBusFactory;
import org.openrsc.editor.model.DisplayConfiguration;
import org.openrsc.editor.model.DisplayConfigurationProperty;
import org.openrsc.editor.model.Tile;
import org.openrsc.editor.model.definition.ColorConstants;
import org.openrsc.editor.model.definition.OverlayDefinition;
import org.openrsc.editor.model.definition.RoofDefinition;
import org.openrsc.editor.model.definition.WallDefinition;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

import static org.openrsc.editor.gui.graphics.EditorCanvas.TILE_SIZE;
import static org.openrsc.editor.model.DisplayConfiguration.DEFAULT_DISPLAY_CONFIGURATION;

public class TileRenderer {
	private static final EventBus eventBus = EventBusFactory.getEventBus();

	private DisplayConfiguration displayConfiguration;

	public TileRenderer() {
		eventBus.register(this);

		this.displayConfiguration = DEFAULT_DISPLAY_CONFIGURATION;
	}

	public void renderTile(Tile tile, Graphics2D g) {
		if (tile == null) {
			return;
		}

		g.setStroke(new BasicStroke(2));
		// paints Tile ground data.
		if (tile.getGroundTextureInt() >= 0) {
			int groundTexture = tile.getGroundTextureInt();
			g.setColor(
				Util.MAP_BRIGHTNESS_LIGHT ? Util.colorArray[groundTexture] : Util.colorArray[groundTexture].darker().darker()
			);
			g.fill(tile.getShape());
			g.draw(tile.getShape());
		}

		// paints Tile ground data (Paths/roads etc, things on top of the original data)
		OverlayDefinition overlayDef = OverlayDefinition.OVERLAYS.get(tile.getGroundOverlayInt());
		if (overlayDef != null) {
			g.setColor(OverlayDefinition.OVERLAYS.get(tile.getGroundOverlayInt()).getColor());
			g.fill(tile.getShape());
			g.draw(tile.getShape());

			// Draw X to let user know this terrain is not passible
			if (!overlayDef.isPassable()) {
				EditorCanvas.drawLine(
					tile,
					LineLocation.DIAGONAL_FROM_TOP_RIGHT,
					ColorConstants.IMPASSIBLE_TERRAIN_OUTLINE);
				EditorCanvas.drawLine(
					tile,
					LineLocation.DIAGONAL_FROM_TOP_LEFT,
					ColorConstants.IMPASSIBLE_TERRAIN_OUTLINE
				);
			}
		}
		// paints Tile wall color (Vertical) + the line to show a wall is there.
		if (WallDefinition.NORMAL.containsKey(tile.getTopBorderWallInt())) {
			EditorCanvas.drawLine(
				tile,
				LineLocation.BORDER_TOP,
				ColorConstants.WALL_OUTLINE_COLOR
			);
		}
		// paints Tile wall color (Horizontal) + line to show a wall is there.
		if (WallDefinition.NORMAL.containsKey(tile.getRightBorderWallInt())) {
			EditorCanvas.drawLine(tile, LineLocation.BORDER_RIGHT, ColorConstants.WALL_OUTLINE_COLOR);
		}
		// paints Diagonal walls.
		if (WallDefinition.NORMAL.containsKey(tile.getDiagonalWalls())) {
			EditorCanvas.drawLine(tile, LineLocation.DIAGONAL_FROM_TOP_RIGHT, ColorConstants.WALL_OUTLINE_COLOR);
		}
		if (WallDefinition.DIAGONAL_BACKWARDS.containsKey(tile.getDiagonalWalls())) {
			EditorCanvas.drawLine(tile, LineLocation.DIAGONAL_FROM_TOP_LEFT, ColorConstants.WALL_OUTLINE_COLOR);
		}

		boolean showRoofs = displayConfiguration.getProperties().get(DisplayConfigurationProperty.SHOW_ROOFS);
		Shape outerRectangle = new Rectangle(
			tile.getX() + 1,
			tile.getY(),
			TILE_SIZE - 1,
			TILE_SIZE - 1
		);
		if (showRoofs && tile.getRoofTexture() != (byte) 0) {
			if (RoofDefinition.ROOFS.containsKey(tile.getRoofTextureInt())) {
				g.setColor(Color.ORANGE);
				g.draw(outerRectangle);
			} else {
				g.setColor(Color.GREEN);
				g.draw(outerRectangle);
			}

		}

		renderPeripherals(tile, g);
	}

	private void renderPeripherals(Tile tile, Graphics2D g) {
		Point rscCoords = Util.getRSCCoords(tile);

		boolean showObjects = displayConfiguration.getProperties().get(DisplayConfigurationProperty.SHOW_OBJECTS);
		boolean showItems = displayConfiguration.getProperties().get(DisplayConfigurationProperty.SHOW_ITEMS);
		boolean showNpcs = displayConfiguration.getProperties().get(DisplayConfigurationProperty.SHOW_NPCS);

		if (showObjects && Util.sceneryLocationMap.get(rscCoords) != null) {
			fillInnerTile(tile, g, Color.CYAN);
		}
		if (showObjects && Util.boundaryLocsMap.get(rscCoords) != null) {
			fillInnerTile(tile, g, Color.CYAN);
		}
		if (showItems && Util.itemLocationMap.get(rscCoords) != null) {
			fillInnerTile(tile, g, Color.RED);
		}
		if (showNpcs && Util.npcLocationMap.get(rscCoords) != null) {
			fillInnerTile(tile, g, Color.YELLOW);
		}
	}

	private void fillInnerTile(Tile tile, Graphics2D g, Color color) {
		int innerInset = 8;
		Shape innerRectangle = new Rectangle(
			tile.getX() + 1 + innerInset / 2,
			tile.getY() + innerInset / 2,
			TILE_SIZE - 1 - innerInset,
			TILE_SIZE - 1 - innerInset
		);

		g.setColor(color);
		g.fill(innerRectangle);
		g.draw(innerRectangle);
	}

	@Subscribe
	public void onDisplayConfigurationChanged(DisplayConfigurationUpdateEvent event) {
		DisplayConfiguration.DisplayConfigurationBuilder configBuilder = this.displayConfiguration.toBuilder();
		event.getUpdatedProperties().forEach(configBuilder::property);
		this.displayConfiguration = configBuilder.build();
	}

}
