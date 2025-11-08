package com.example.application.views.gridwithfilters;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Storage Location")
@Route("storage-location")
@Menu(order = 1, icon = LineAwesomeIconUrl.STORE_SOLID) // erscheint unter „Logistic“
@Uses(Icon.class)
public class StorageLocationView extends Div {

}
