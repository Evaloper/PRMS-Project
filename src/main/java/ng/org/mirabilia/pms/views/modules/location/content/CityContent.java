package ng.org.mirabilia.pms.views.modules.location.content;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import ng.org.mirabilia.pms.domain.entities.City;
import ng.org.mirabilia.pms.domain.entities.Property;
import ng.org.mirabilia.pms.domain.entities.State;
import ng.org.mirabilia.pms.services.CityService;
import ng.org.mirabilia.pms.services.StateService;
import ng.org.mirabilia.pms.views.Utils.CityInfoDialog;
import ng.org.mirabilia.pms.views.forms.location.city.AddCityForm;
import ng.org.mirabilia.pms.views.forms.location.city.EditCityForm;

import java.util.List;

public class CityContent extends VerticalLayout {

    private final CityService cityService;
    private final StateService stateService;
    private final Grid<City> cityGrid;
    private final TextField searchField;
    private final ComboBox<State> stateComboBox;

    public CityContent(CityService cityService, StateService stateService) {
        this.cityService = cityService;
        this.stateService = stateService;

        setSpacing(true);
        setPadding(false);
        addClassName("city-content");

        searchField = new TextField();
        searchField.setPlaceholder("Search City");
        searchField.setClearButtonVisible(true);
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.addClassName("custom-search-field");
        searchField.addClassName("custom-toolbar-field");

        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> updateGrid());

        stateComboBox = new ComboBox<>("State");
        stateComboBox.setItemLabelGenerator(State::getName);
        stateComboBox.setItems(stateService.getAllStates());
        stateComboBox.setClearButtonVisible(true);
        stateComboBox.addValueChangeListener(e -> updateGrid());
        stateComboBox.addClassNames("custom-filter");

        Button resetButton = new Button(new Icon(VaadinIcon.REFRESH));
        resetButton.addClassName("custom-button");
        resetButton.addClassName("custom-reset-button");
        resetButton.addClassName("custom-toolbar-button");
        resetButton.addClickListener(e -> resetFilters());

        Button addCityButton = new Button("Add City");
        addCityButton.addClassName("custom-button");
        addCityButton.addClassName("custom-add-button");
        addCityButton.addClassName("custom-toolbar-button");
        addCityButton.setPrefixComponent(new Icon(VaadinIcon.PLUS));

        cityGrid = new Grid<>(City.class, false);
        cityGrid.addClassName("custom-grid");
        Grid.Column<City> stateColumn = cityGrid.addColumn((city)-> city.getState().getName())
                .setHeader("State");
        Grid.Column<City> cityColumn = cityGrid.addColumn(City::getName)
                .setHeader("City");
        Grid.Column<City> cityCodeColumn = cityGrid.addColumn(City::getCityCode)
                .setHeader("City Id");

        cityGrid.setColumnOrder(stateColumn,cityColumn,cityCodeColumn);

        cityGrid.asSingleSelect().addValueChangeListener(event -> {
            City selectedCity = event.getValue();
            if (selectedCity != null ) {

                openCityInfoDialog(selectedCity);
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(searchField, stateComboBox, resetButton, addCityButton);
        toolbar.setWidthFull();
        toolbar.addClassName("custom-toolbar");

        add(toolbar, cityGrid);

        addCityButton.addClickListener(e -> openAddCityDialog());

        updateGrid();
    }

    private void openCityInfoDialog(City selectedCity) {
        CityInfoDialog cityInfoDialog = new CityInfoDialog(
                selectedCity,
                ()->{
                    if(selectedCity.getPhases().isEmpty())
                        openEditCityDialog(selectedCity);
                    else {
                        Notification.show("Cannot Edit City with Phases",3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
        cityInfoDialog.open();
    }

    private void updateGrid() {
        String keyword = searchField.getValue();
        State selectedState = stateComboBox.getValue();

        List<City> cities;
        if ((keyword == null || keyword.isEmpty()) && selectedState == null) {
            cities = cityService.getAllCities();
        } else if (selectedState != null && (keyword == null || keyword.isEmpty())) {
            cities = cityService.filterCitiesByState(selectedState.getId());
        } else if (selectedState != null) {
            cities = cityService.searchCityByKeywordsAndState(keyword, selectedState.getId());
        } else {
            cities = cityService.searchCityByKeywords(keyword);
        }

        cityGrid.setItems(cities);
    }

    private void resetFilters() {
        searchField.clear();
        stateComboBox.clear();
        updateGrid();
    }

    private void openAddCityDialog() {
        AddCityForm cityForm = new AddCityForm(cityService, stateService, (v) -> updateGrid());
        cityForm.open();
    }

    private void openEditCityDialog(City city) {
        EditCityForm editCityForm = new EditCityForm(cityService, stateService, city, (v) -> updateGrid());
        editCityForm.open();
    }
}
