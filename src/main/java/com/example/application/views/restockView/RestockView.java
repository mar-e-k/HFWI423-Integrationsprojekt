package com.example.application.views.restockView;

import com.vaadin.flow.data.provider.ListDataProvider;
import org.springframework.data.domain.Sort;
import com.example.application.data.stockChangeLog.StockChangeLog;
import com.example.application.services.StockChangeLogService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Restock")
@Route("restock")
@Menu(order = 6, icon = LineAwesomeIconUrl.WINDOW_RESTORE)
@Uses(Icon.class)
public class RestockView extends Div {

}
