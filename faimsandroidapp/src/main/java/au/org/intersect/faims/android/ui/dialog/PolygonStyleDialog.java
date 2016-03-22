package au.org.intersect.faims.android.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import au.org.intersect.faims.android.constants.FaimsSettings;
import au.org.intersect.faims.android.log.FLog;
import au.org.intersect.faims.android.nutiteq.GeometryStyle;

public class PolygonStyleDialog extends StyleDialog {
	
	public static class Builder extends StyleDialog.Builder {
		
		public Builder(Context context, GeometryStyle style) {
			super(context, style);
		}

		@Override
		public SettingsDialog createDialog() {
			final PolygonStyleDialog d = new PolygonStyleDialog(context);
			final GeometryStyle style = this.style;
			d.setStyle(style);
			
			setTitle("Style Settings");
			addRange("minZoom", "Min Zoom:", style.minZoom, 0, FaimsSettings.MAX_ZOOM);
			addColorField("color", "Polygon Fill Color:", Integer.toHexString(style.polygonColor));
			addCheckBox("showStroke", "Show Outline on Polygon:", style.showStroke);
			addColorField("strokeColor", "Outline Color:", Integer.toHexString(style.lineColor));
			addSlider("width", "Outline Width:", style.width);
			addSlider("pickingWidth", "Outline Selection Tolerance:", style.pickingWidth);
			addCheckBox("showPoints", "Show Vertices on Polygon:", style.showPoints);
			addSlider("size", "Vertex Size:", style.size);
			addSlider("pickingSize", "Vertex Selection Tolerance:", style.pickingSize);
			
			if (this.positiveListener == null) {
				setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							int minZoom = d.parseRange("minZoom", 0, FaimsSettings.MAX_ZOOM);
							int color = d.parseColor("color");
							float size = d.parseSlider("size");
							float pickingSize = d.parseSlider("pickingSize");
							int lineColor = d.parseColor("strokeColor");
							float width = d.parseSlider("width");
							float pickingWidth = d.parseSlider("pickingWidth");
							boolean showStroke = d.parseCheckBox("showStroke");
							boolean showPoints = d.parseCheckBox("showPoints");
							
							style.minZoom = minZoom;
							style.pointColor = lineColor;
							style.lineColor = lineColor;
							style.polygonColor = color;
							style.size = size;
							style.pickingSize = pickingSize;
							style.width = width;
							style.pickingWidth = pickingWidth;
							style.showStroke = showStroke;
							style.showPoints = showPoints;
						} catch (Exception e) {
							FLog.e(e.getMessage(), e);
							d.showError(e.getMessage());
						}
					}
				});
			}
			
			if (this.negativeListener == null) {
				setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// ignore
					}
				});
			}
			
			return d;
		}
		
	}
	
	protected PolygonStyleDialog(Context context) {
		super(context);
	}

}
