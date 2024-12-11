package de.mhus.pallaver.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.mhus.pallaver.generator.Generator;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Route(value = "generator", layout = MainLayout.class)
@PageTitle("Generator")
public class GeneratorView extends VerticalLayout {

    private MyModelItem selectedModelItem;
    @Autowired
    private ModelService modelService;
    private ChatPanel chatHistory;
    private Span infoText;
    @Autowired(required = false)
    private List<Generator> generators;
    private ListBox<Item> generatorList;

    @PostConstruct
    public void init() {
        var menuBar = createMenuBar();

        var generatorListLayout = new VerticalLayout();
        generatorListLayout.setSizeFull();
        generatorList = createCheckList();
        generatorListLayout.add(generatorList);

        infoText = new Span("");
        chatHistory = new ChatPanel();
        chatHistory.setSizeFull();

        var splitLayout = new SplitLayout(generatorListLayout, chatHistory);
        splitLayout.setSizeFull();
        splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
        splitLayout.setSplitterPosition(20);

        add(menuBar, infoText, splitLayout);
        setSizeFull();

        updateModelText();
    }

    private ListBox<Item> createCheckList() {
        var list = new ListBox<Item>();
        list.setSizeFull();
        List<Item> generatorsList = new ArrayList<>();
        list.setItemLabelGenerator(Item::name);
        ListDataProvider<Item> generatorListDataProvider = new ListDataProvider<Item>(generatorsList);
        list.setDataProvider(generatorListDataProvider);
        if (generators != null)
            generators.forEach(check -> generatorsList.add(new Item(check.getTitle(), check)));
        generatorListDataProvider.refreshAll();
        return list;
    }

    private MenuBar createMenuBar() {
        var menuBar = new MenuBar();
        menuBar.addItem(VaadinIcon.RECYCLE.create(), e -> actionReset());
        var menuModel = menuBar.addItem("Model").getSubMenu();
        modelService.getModels().forEach(model -> {
            var item = menuModel.addItem(model.getTitle());
            new MyModelItem(model, item);
        });
        menuBar.addItem("Run", e -> actionRun());
        return menuBar;
    }

    private void actionRun() {
        chatHistory.clear();
        UI ui = UI.getCurrent();
        if (selectedModelItem == null) {
            chatHistory.addBubble("No model selected", true, ChatPanel.COLOR.RED);
            return;
        }
        var model = selectedModelItem.getModel();
        var generator = generatorList.getValue().generator();

        chatHistory.addBubble(model.getTitle() + " - " + generator.getTitle(), true, ChatPanel.COLOR.BLUE);

        Thread.startVirtualThread(() -> {
            actionRun(ui, model, generator);
        });
    }

    private void actionRun(UI ui, LLModel model, Generator generator) {
        var monitor = new GeneratorMonitor(ui, chatHistory);
        Thread.startVirtualThread(() -> {
            try {
                generator.run(model, monitor);
            } catch (Exception e) {
                LOGGER.error("Error", e);
                monitor.reportError(e);
            }
        });
    }

    private void actionReset() {
        updateModelText();
        chatHistory.clear();
    }

    private void updateModelText() {
        infoText.setText("Models: " + (selectedModelItem == null ? "" : selectedModelItem.getTitle()));
    }

    @Getter
    private class MyModelItem {
        private final LLModel model;
        MenuItem item;
        @Setter
        private ChatPanel.COLOR color = ChatPanel.COLOR.GREEN;

        public MyModelItem(LLModel model, MenuItem item) {
            this.model = model;
            this.item = item;

            item.addClickListener(e -> {
                selectedModelItem = this;
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

    private record Item(String name, Generator generator) {
    }
}

