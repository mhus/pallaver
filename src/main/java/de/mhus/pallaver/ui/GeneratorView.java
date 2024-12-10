package de.mhus.pallaver.ui;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.mhus.pallaver.model.ModelService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Route(value = "generator", layout = MainLayout.class)
@PageTitle("Generator")
public class GeneratorView extends VerticalLayout {

    @Autowired
    private ModelService modelService;

    @PostConstruct
    public void init() {

    }

}
