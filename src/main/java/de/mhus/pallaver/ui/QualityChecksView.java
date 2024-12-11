package de.mhus.pallaver.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import de.mhus.pallaver.quality.QualityCheck;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Route(value = "quality", layout = MainLayout.class)
@PageTitle("Quality Checks")
public class QualityChecksView extends VerticalLayout {

    @Autowired
    private ModelService modelService;
    private ChatPanel chatHistory;
    private Span infoText;
    private final List<MyModelItem> models = new ArrayList<>();
    @Autowired(required = false)
    private List<QualityCheck> qualityChecks;
    private MultiSelectListBox<Check> checkList;
    private ListDataProvider<Check> checkListDataProvider;

    @PostConstruct
    public void init() {
        var menuBar = createMenuBar();

        var checkListLayout = new VerticalLayout();
        checkListLayout.setSizeFull();
        checkList = createCheckList();
        checkListLayout.add(checkList);

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

    private MultiSelectListBox<Check> createCheckList() {
        var list = new MultiSelectListBox<Check>();
        list.setSizeFull();
        List<Check> checkList = new ArrayList<>();
        list.setItemLabelGenerator(Check::name);
        checkListDataProvider = new ListDataProvider<Check>(checkList);
        list.setDataProvider(checkListDataProvider);
        if (qualityChecks != null)
            qualityChecks.forEach(check -> checkList.add(new Check(check.getTitle(), check)));
        checkListDataProvider.refreshAll();
        return list;
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
        chatHistory.clear();
        UI ui = UI.getCurrent();
        Thread.startVirtualThread(() -> {
            checkList.getSelectedItems().forEach(c -> {
                ui.access(() -> {
                    var resultBubble = chatHistory.addBubble(c.name(), true, ChatPanel.COLOR.BLUE);
                    actionRun(resultBubble, ui, c);
                });
            });
        });
    }

    private void actionRun(ChatBubble resultBubble, UI ui, Check c) {
        ColorRotator colorRotator = new ColorRotator();
        List<CompletableFuture<Boolean>> completed = new ArrayList<>();
        models.stream().filter(MyModelItem::isEnabled).forEach(m -> {
            completed.add(actionRun(ui, c, m, colorRotator.next()));
        });
        CompletableFuture.allOf(completed.toArray(new CompletableFuture[0])).thenRun(() -> {
            ui.access(() -> {
                LOGGER.info("Completed {}", c.name());
                resultBubble.setText(
                    completed.stream().map(f -> f.getNow(false) + " ").reduce("", String::concat)
                );
                chatHistory.scrollToEnd();
            });
        });
    }

    private CompletableFuture<Boolean> actionRun(UI ui, Check c, MyModelItem m, ChatPanel.COLOR color) {
        var monitor = new BubbleMonitor(ui, m.getTitle() ,color);
        ui.access(() -> chatHistory.addBubble(monitor));
        var future = new CompletableFuture<Boolean>();
        Thread.startVirtualThread(() -> {
            try {
                c.check().run(m.getModel(), monitor);

            } catch (Exception e) {
                LOGGER.error("Error", e);
                monitor.reportError(e);
            }
            future.complete(monitor.isSuccess());
        });
        return future;
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

    private record Check(String name, QualityCheck check) {
    }
}

