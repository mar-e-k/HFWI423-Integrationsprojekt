package com.example.application.views.storageLocationView;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Storage Location Changes")
@Route("storage-location-changes")
@Menu(order = 4, icon = LineAwesomeIconUrl.HISTORY_SOLID) // erscheint unter „Logistic“
@Uses(Icon.class)

public class StorageLocationChangeLogView extends Div {


}
