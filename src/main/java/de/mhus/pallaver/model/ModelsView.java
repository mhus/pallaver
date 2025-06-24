package de.mhus.pallaver.model;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.mhus.commons.tools.MLang;
import de.mhus.pallaver.lltype.UnknownType;
import de.mhus.pallaver.lltype.LLType;
import de.mhus.pallaver.ui.MainLayout;
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
    private TextField modelModel;
    private TextField modelUrl;
    private PasswordField modelApiKey;

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
            t.getDefaultModels().forEach(m -> {
                if (modelListDataProviderList.stream().filter(mm -> mm.getType().equals(m.getType()) && mm.getModel().equals(m.getModel()) ).findFirst().isEmpty()) {
                    modelListDataProviderList.add(m);
                }
            });
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
            modelModel.clear();
            modelModel.setEnabled(false);
            modelUrl.clear();
            modelUrl.setEnabled(false);
            modelApiKey.clear();
            modelApiKey.setEnabled(false);
            modelIsDefault.setValue(false);
            modelIsDefault.setEnabled(false);
        } else {
            modelTitle.setValue(item.getTitle());
            modelTitle.setEnabled(true);
            modelType.setValue(typeList.stream().filter(i -> i.getName().equals(item.getType())).findFirst().orElseGet(() -> new UnknownType(item.getType()) ) );
            modelType.setEnabled(true);
            MLang.tryThis(() -> modelModel.setValue(item.getModel())).orGet(() -> {modelModel.clear(); return null; } ); ;
            modelModel.setEnabled(true);
            modelUrl.setValue(item.getUrl());
            modelUrl.setEnabled(true);
            modelApiKey.setValue(item.getApiKey());
            modelApiKey.setEnabled(true);
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
        modelModel = new TextField("Model");
        modelModel.setEnabled(false);
        modelModel.addValueChangeListener(e -> {
            if (modelItem != null) {
                modelItem.setModel(e.getValue());
            }
        });
        modelUrl = new TextField("Url");
        modelUrl.setEnabled(false);
        modelUrl.addValueChangeListener(e -> {
            if (modelItem != null) {
                modelItem.setUrl(e.getValue());
            }
        });
        modelApiKey = new PasswordField("Api Key");
        modelApiKey.setEnabled(false);
        modelApiKey.addValueChangeListener(e -> {
            if (modelItem != null) {
                modelItem.setApiKey(e.getValue());
            }
        });
        modelIsDefault = new Checkbox("Is Default");
        modelIsDefault.addValueChangeListener(e -> {
            if (modelItem != null) {
                modelItem.setDefault(e.getValue());
            }
        });

        formLayout.add(modelTitle, modelType, modelModel, modelUrl, modelApiKey, modelIsDefault);
        formLayout.setSizeFull();
        formLayout.addClassNames(LumoUtility.Padding.Horizontal.MEDIUM);
        return formLayout;
    }

}
