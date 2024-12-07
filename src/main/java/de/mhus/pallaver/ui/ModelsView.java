package de.mhus.pallaver.ui;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;

@Route(value = "models", layout = MainLayout.class)
@PageTitle("Models")
public class ModelsView extends VerticalLayout {

    @PostConstruct
    public void init() {
        add("Models View");
    }
    
}
