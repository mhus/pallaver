package de.mhus.pallaver.ui;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.mhus.pallaver.lltype.UnknownType;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.LLType;
import de.mhus.pallaver.model.ModelService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Route(value = "models", layout = MainLayout.class)
@PageTitle("Models")
public class ModelsView extends VerticalLayout {

    @Autowired
    private ModelService modelService;
    private final List<LLType> typeList = new ArrayList<>();
    private ListDataProvider<LLModel> modelListDataProvider ;
    private List<LLModel> modelListDataProviderList;
    private ListBox<LLModel> modelListBox;
    private TextField modelTitle;
    private ComboBox<LLType> modelType;
    private LLModel modelItem;
    private Checkbox modelIsDefault;

    @PostConstruct
    public void init() {
        var actions = createActions();
        var list = createModelList();
        var detailsForm = createDetailsForm();
        var listLayout = new VerticalLayout(actions, list);
        listLayout.setSizeFull();
        var splitLayout = new SplitLayout(listLayout, detailsForm);
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(20);
        add(splitLayout);
        setSizeFull();

    }

    private MenuBar createActions() {
        var menuBar =  new MenuBar();
        menuBar.addItem(VaadinIcon.PLUS.create(), e -> actionCreateModel());
        menuBar.addItem(VaadinIcon.TRASH.create(), e -> actionRemoveModel());
        menuBar.addItem(VaadinIcon.SAFE.create(), e -> actionSaveModel());
        menuBar.addItem(VaadinIcon.GLOBE.create(), e -> actionGenerateAll());
        menuBar.addItem(VaadinIcon.REFRESH.create(), e -> actionLoad());

        return menuBar;
    }

    private void actionLoad() {
        modelListDataProviderList.clear();
        modelListDataProviderList.addAll(modelService.getModels());
        modelListDataProvider.refreshAll();
    }

    private void actionGenerateAll() {
        modelService.getModelTypes().forEach(t -> {
            if (modelListDataProviderList.stream().filter(m -> m.getType().equals(t.getName())).findFirst().isEmpty()) {
                var newItem = new LLModel();
                newItem.setType(t.getName());
                newItem.setTitle(t.getTitle());
                modelListDataProviderList.add(newItem);
            }
        });
        modelListDataProvider.refreshAll();
    }

    private void actionSaveModel() {
        modelService.setModels(modelListDataProviderList);
    }

    private void actionRemoveModel() {
        modelListDataProviderList.remove(
                modelListBox.getValue()
        );
        modelListDataProvider.refreshAll();
        setSelectedForm(null);
    }

    private void actionCreateModel() {
        var newItem = new LLModel();
        newItem.setType(!typeList.isEmpty() ? "" : typeList.getFirst().getName());
        newItem.setTitle("+++ New Model");
        modelListDataProviderList.add(newItem);
        modelListDataProvider.refreshAll();
        modelListBox.setValue(newItem);
    }

    private void setSelectedForm(LLModel item) {
        modelItem = item;
        if (item == null) {
            modelTitle.clear();
            modelTitle.setEnabled(false);
            modelType.clear();
            modelType.setEnabled(false);
            modelIsDefault.setValue(false);
            modelIsDefault.setEnabled(false);
        } else {
            modelTitle.setValue(item.getTitle());
            modelTitle.setEnabled(true);
            modelType.setValue(typeList.stream().filter(i -> i.getName().equals(item.getType())).findFirst().orElseGet(() -> new UnknownType(item.getType()) ) );
            modelType.setEnabled(true);
            modelIsDefault.setValue(item.isDefault());
            modelIsDefault.setEnabled(true);
        }
    }

    private ListBox<LLModel> createModelList() {
        modelListBox = new ListBox<LLModel>();
        modelListDataProviderList = new ArrayList<>();
        modelListDataProvider = new ListDataProvider<LLModel>(modelListDataProviderList);
        modelListDataProviderList.addAll(modelService.getModels());
        modelListDataProvider.refreshAll();
        modelListBox.setDataProvider(modelListDataProvider);
        modelListBox.setItemLabelGenerator(LLModel::getTitle);
        modelListBox.setSizeFull();
        modelListBox.addValueChangeListener(e -> setSelectedForm(e.getValue()));
        return modelListBox;
    }

    private FormLayout createDetailsForm() {
        var formLayout = new FormLayout();
        modelTitle = new TextField("Title");
        modelTitle.setEnabled(false);
        modelTitle.addValueChangeListener(e -> {
            if (modelItem != null) {
                modelItem.setTitle(e.getValue());
                modelListDataProvider.refreshItem(modelItem);
            }
        });
        modelType = new ComboBox<LLType>("Model Type");
        typeList.addAll(modelService.getModelTypes());
        modelType.setItems(typeList);
        modelType.setItemLabelGenerator(LLType::getTitle);
        modelType.setAllowCustomValue(true);
        modelType.addCustomValueSetListener(e -> {
            var value = e.getDetail();
            if (value != null) {
                var newType =  new UnknownType(value);
                typeList.add(newType);
                modelType.setItems(typeList);
                modelType.setValue(newType);
            }
        });
        modelType.addValueChangeListener(e -> {
            if (modelItem != null) {
                modelItem.setType(e.getValue().getName());
            }
        });
        modelIsDefault = new Checkbox("Is Default");
        modelIsDefault.addValueChangeListener(e -> {
            if (modelItem != null) {
                modelItem.setDefault(e.getValue());
            }
        });

        formLayout.add(modelTitle, modelType, modelIsDefault);
        formLayout.setSizeFull();
        formLayout.addClassNames(LumoUtility.Padding.Horizontal.MEDIUM);
        return formLayout;
    }

}
