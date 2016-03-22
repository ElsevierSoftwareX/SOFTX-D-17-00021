package au.org.intersect.faims.android.ui.map;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import au.org.intersect.faims.android.log.FLog;
import au.org.intersect.faims.android.nutiteq.GeometryStyle;
import au.org.intersect.faims.android.ui.dialog.ErrorDialog;
import au.org.intersect.faims.android.ui.dialog.LineStyleDialog;
import au.org.intersect.faims.android.ui.dialog.PointStyleDialog;
import au.org.intersect.faims.android.ui.dialog.PolygonStyleDialog;

public class SelectionDialog extends AlertDialog {
	
	private class SelectionAdapter extends BaseAdapter {
		
		private List<GeometrySelection> selections;
		private ArrayList<View> itemViews;

		public SelectionAdapter(List<GeometrySelection> selections) {
			this.selections = selections;
			this.itemViews = new ArrayList<View>();
			
			for (GeometrySelection set : selections) {
				SelectionListItem item = new SelectionListItem(SelectionDialog.this.getContext());
				item.init(listView,set, mapView);
				itemViews.add(item);
			} 
		}
		
		@Override
		public int getCount() {
			return selections.size();
		}

		@Override
		public Object getItem(int position) {
			return selections.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View arg1, ViewGroup arg2) {
			SelectionListItem item = (SelectionListItem) itemViews.get(position);
			if(SelectionDialog.this.mapView.getSelectedSelection() != null){
				if(SelectionDialog.this.mapView.getSelectedSelection() .equals(item.getSelection())){
					for(View itemView : itemViews){
						SelectionListItem layerItem = (SelectionListItem) itemView;
						layerItem.setItemSelected(false);
					}
					item.setItemSelected(true);
				}
			}else{
				for(View itemView : itemViews){
					SelectionListItem layerItem = (SelectionListItem) itemView;
					layerItem.setItemSelected(false);
				}
			}
			return item;
		}
		
	}

	private LinearLayout layout;
	private ListView listView;
	private CustomMapView mapView;
	protected PointStyleDialog pointStyleDialog;
	protected LineStyleDialog lineStyleDialog;
	protected PolygonStyleDialog polygonStyleDialog;

	protected SelectionDialog(Context context) {
		super(context);
	}

	public void attachToMap(CustomMapView mapView) {
		this.mapView = mapView;
		
		layout = new LinearLayout(this.getContext());
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		this.setView(layout);
		
		createAddButton();
		createListView();
		
		redrawSelection();
	}
	
	private void createListView() {
		listView = new ListView(this.getContext(),null);
		listView.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				try {
					SelectionListItem item = (SelectionListItem) view;
					if(!item.isRestricted()){
						List<GeometrySelection> selections = mapView.getSelections();
						mapView.setSelectedSelection(selections.get(position));
						mapView.setSelectionActive(selections.get(position), true);
						((SelectionAdapter)listView.getAdapter()).getView(position, view, listView);
					}
				} catch (Exception e) {
					FLog.e(e.getMessage(), e);
					showErrorDialog(e.getMessage());
				}
			}
			
		});
		
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int position, long arg3) {
				List<GeometrySelection> dataSets = mapView.getSelections();
				final GeometrySelection set = dataSets.get(position);
				
				Context context = SelectionDialog.this.getContext();
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Data Set Options");

				LinearLayout layout = new LinearLayout(context);
				layout.setOrientation(LinearLayout.VERTICAL);
				
				builder.setView(layout);
				builder.setPositiveButton("Done", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Close the dialog
					}
				});

				final Dialog d = builder.create();
				
				Button removeButton = new Button(context);
				removeButton.setText("Remove");
				removeButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						d.dismiss();
						removeSelection(set);
					}
					
				});
				
				Button renameButton = new Button(context);
				renameButton.setText("Rename");
				renameButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						d.dismiss();
						renameSelection(set);
					}
					
				});
				
				Button pointStyleButton = createPointStyleButton(set.getPointStyle());
				Button lineStyleButton = createLineStyleButton(set.getLineStyle());
				Button polygonStyleButton = createPolygonStyleButton(set.getPolygonStyle());
				Button restrictionButton = new Button(context);
				final SelectionListItem item = (SelectionListItem) v;
				restrictionButton.setText(item.isRestricted() ? "Remove from restriction" : "Add to restriction");
				restrictionButton.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						d.dismiss();
						mapView.addRestrictedSelection(set,!item.isRestricted());
						item.setRestricted(!item.isRestricted());
						mapView.updateSelections();
					}
				});
				
				layout.addView(removeButton);
				layout.addView(renameButton);
				layout.addView(pointStyleButton);
				layout.addView(lineStyleButton);
				layout.addView(polygonStyleButton);
				if(!item.isSelected()){
					layout.addView(restrictionButton);
				}
				d.setCanceledOnTouchOutside(true);
				d.show();
				return true;
			}
			
		});

		layout.addView(listView);
	}

	private void createAddButton() {
		Button addButton = new Button(this.getContext());
		addButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
		addButton.setText("Add");
		addButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				addSelection();
			}
			
		});
		
		layout.addView(addButton);
	}
	
	public void redrawSelection() {
		List<GeometrySelection> selections = mapView.getSelections();
		listView.setAdapter(new SelectionAdapter(selections));
	}
	
	public void addSelection() {
		AlertDialog.Builder builder = new AlertDialog.Builder(SelectionDialog.this.getContext());
		
		builder.setTitle("Selection Manager");
		builder.setMessage("Add selection:");
		
		ScrollView scrollView = new ScrollView(this.getContext());
		LinearLayout layout = new LinearLayout(this.getContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		scrollView.addView(layout);
		
		builder.setView(scrollView);
		
		TextView textView = new TextView(this.getContext());
		textView.setText("Selection name:");
		layout.addView(textView);
		final EditText editText = new EditText(SelectionDialog.this.getContext());
		layout.addView(editText);
		
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				try {
					mapView.addSelection(editText.getText().toString());
					redrawSelection();
				} catch (Exception e) {
					FLog.e(e.getMessage(), e);
					showErrorDialog(e.getMessage());
				}
			}
	        
	    });
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int id) {
	           // ignore
	        }
	    });
		
		AlertDialog d = builder.create();
		d.setCanceledOnTouchOutside(true);
		d.show();
	}
	
	public void removeSelection(GeometrySelection set) {
		mapView.removeSelection(set.getName());
		redrawSelection();
	}
	
	public void renameSelection(final GeometrySelection set) {
		AlertDialog.Builder builder = new AlertDialog.Builder(SelectionDialog.this.getContext());
		
		builder.setTitle("Selection Manager");
		builder.setMessage("Rename selection:");
		
		ScrollView scrollView = new ScrollView(this.getContext());
		LinearLayout layout = new LinearLayout(this.getContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		scrollView.addView(layout);
		
		builder.setView(scrollView);
		
		TextView textView = new TextView(this.getContext());
		textView.setText("Selection name:");
		layout.addView(textView);
		final EditText editText = new EditText(SelectionDialog.this.getContext());
		layout.addView(editText);
		editText.setText(set.getName());
		
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				try {
					mapView.renameSelection(editText.getText().toString(), set);
					redrawSelection();
				} catch (Exception e) {
					FLog.e(e.getMessage(), e);
					showErrorDialog(e.getMessage());
				}
			}
	        
	    });
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int id) {
	           // ignore
	        }
	    });
		
		AlertDialog d = builder.create();
		d.setCanceledOnTouchOutside(true);
		d.show();
	}
	
	private void showErrorDialog(String message) {
		new ErrorDialog(SelectionDialog.this.getContext(), "Selection Manager Error", message).show();
	}
	
	public Button createPointStyleButton(final GeometryStyle pointStyle){
		Button button = new Button(this.getContext());
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layoutParams.weight = 1;
		button.setLayoutParams(layoutParams);
		button.setText("Style Point");
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				PointStyleDialog.Builder builder = new PointStyleDialog.Builder(SelectionDialog.this.getContext(), pointStyle);
				builder.setDismissListener(new DialogInterface.OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						mapView.updateSelections();
					}
				});
				pointStyleDialog = (PointStyleDialog) builder.create();
				pointStyleDialog.show();
			}
				
		});
		return button;
	}
	
	public Button createLineStyleButton(final GeometryStyle lineStyle){
		Button button = new Button(this.getContext());
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layoutParams.weight = 1;
		button.setLayoutParams(layoutParams);
		button.setText("Style Line");
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				LineStyleDialog.Builder builder = new LineStyleDialog.Builder(SelectionDialog.this.getContext(), lineStyle);
				builder.setDismissListener(new DialogInterface.OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						mapView.updateSelections();
					}
				});
				lineStyleDialog = (LineStyleDialog) builder.create();
				lineStyleDialog.show();
			}
				
		});
		return button;
	}

	public Button createPolygonStyleButton(final GeometryStyle polygonStyle){
		Button button = new Button(this.getContext());
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layoutParams.weight = 1;
		button.setLayoutParams(layoutParams);
		button.setText("Style Polygon");
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				PolygonStyleDialog.Builder builder = new PolygonStyleDialog.Builder(SelectionDialog.this.getContext(), polygonStyle);
				builder.setDismissListener(new DialogInterface.OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						mapView.updateSelections();
					}
				});
				polygonStyleDialog = (PolygonStyleDialog) builder.create();
				polygonStyleDialog.show();
			}
				
		});
		return button;
	}

}
