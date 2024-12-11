package de.mhus.pallaver.ui;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Route(value = "quality", layout = MainLayout.class)
@PageTitle("Quality Checks")
public class QualityChecksView extends VerticalLayout {

    @Autowired
    private ModelService modelService;
    private ChatPanel chatHistory;
    private Span infoText;
    private final List<MyModelItem> models = new ArrayList<>();

    @PostConstruct
    public void init() {
        var menuBar = createMenuBar();

        var checkListLayout = new VerticalLayout();
        checkListLayout.setSizeFull();

        infoText = new Span("");
        chatHistory = new ChatPanel();
        chatHistory.setSizeFull();

        var splitLayout = new SplitLayout(checkListLayout, chatHistory);
        splitLayout.setSizeFull();
        splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
        splitLayout.setSplitterPosition(20);

        add(menuBar, infoText, splitLayout);
        setSizeFull();

        models.stream().filter(MyModelItem::isDefault).forEach(m -> {
            m.item.setChecked(true);
            m.enabled = true;
        });
        updateModelText();
    }

    private MenuBar createMenuBar() {
        var menuBar = new MenuBar();
        menuBar.addItem(VaadinIcon.RECYCLE.create(), e -> actionReset());
        var menuModel = menuBar.addItem("Model").getSubMenu();
        modelService.getModels().forEach(model -> {
            var item = menuModel.addItem(model.getTitle());
            item.setCheckable(true);
            item.setChecked(false);
            models.add(new MyModelItem(model, item));
        });
        menuBar.addItem("Run", e -> actionRun());
        return menuBar;
    }

    private void actionRun() {
    }

    private void actionReset() {
        updateModelText();
        chatHistory.clear();
    }

    private void updateModelText() {
        final StringBuffer text = new StringBuffer();
        models.stream().filter(MyModelItem::isEnabled).forEach(m -> text.append(m.getTitle()).append(" "));
        infoText.setText("Models: " + text);
    }

    @Getter
    private class MyModelItem {
        private final LLModel model;
        private boolean enabled;
        MenuItem item;
        @Setter
        private ChatPanel.COLOR color = ChatPanel.COLOR.GREEN;

        public MyModelItem(LLModel model, MenuItem item) {
            this.model = model;
            this.item = item;

            item.addClickListener(e -> {
                enabled = item.isChecked();
                updateModelText();
            });
        }

        public String getTitle() {
            return model.getTitle();
        }

        public boolean isDefault() {
            return model.isDefault();
        }
    }
}

