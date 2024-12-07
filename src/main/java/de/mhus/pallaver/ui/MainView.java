package de.mhus.pallaver.ui;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Main")
public class MainView extends VerticalLayout {

    @PostConstruct
    public void init() {
        add("Main View");
    }

}
