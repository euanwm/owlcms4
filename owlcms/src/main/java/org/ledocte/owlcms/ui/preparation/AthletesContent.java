/***
 * Copyright (c) 2018-2019 Jean-François Lamy
 * 
 * This software is licensed under the the Apache 2.0 License amended with the
 * Commons Clause.
 * License text at https://github.com/jflamy/owlcms4/master/License
 * See https://redislabs.com/wp-content/uploads/2018/10/Commons-Clause-White-Paper.pdf
 */
package org.ledocte.owlcms.ui.preparation;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

import org.ledocte.owlcms.crudui.Bindable;
import org.ledocte.owlcms.data.athlete.Athlete;
import org.ledocte.owlcms.data.athlete.AthleteRepository;
import org.ledocte.owlcms.data.athlete.Gender;
import org.ledocte.owlcms.data.category.AgeDivision;
import org.ledocte.owlcms.data.category.Category;
import org.ledocte.owlcms.data.category.CategoryRepository;
import org.ledocte.owlcms.data.group.Group;
import org.ledocte.owlcms.data.group.GroupRepository;
import org.ledocte.owlcms.ui.crudui.OwlcmsCrudFormFactory;
import org.ledocte.owlcms.ui.crudui.OwlcmsCrudLayout;
import org.ledocte.owlcms.ui.crudui.OwlcmsGridCrud;
import org.ledocte.owlcms.ui.home.ContentWrapping;
import org.slf4j.LoggerFactory;
import org.vaadin.crudui.crud.CrudListener;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.Binding;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.validator.DoubleRangeValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Class AthleteContent
 * 
 * Defines the toolbar and the table for editing data for the athletes.
 * 
 */
@SuppressWarnings("serial")
@Route(value = "preparation/athletes", layout = AthletesLayout.class)
public class AthletesContent extends VerticalLayout
		implements CrudListener<Athlete>, ContentWrapping {
	
	final private static Logger logger = (Logger)LoggerFactory.getLogger(AthletesContent.class);
	static {logger.setLevel(Level.DEBUG);}

	private TextField lastNameFilter = new TextField();
	private ComboBox<AgeDivision> ageDivisionFilter = new ComboBox<>();
	private ComboBox<Category> categoryFilter = new ComboBox<>();
	private ComboBox<Group> groupFilter = new ComboBox<>();
	private Checkbox weighedInFilter = new Checkbox();

	/**
	 * Instantiates a new athlete editing table.
	 */
	public AthletesContent() {
		OwlcmsCrudFormFactory<Athlete> crudFormFactory = createFormFactory();
		GridCrud<Athlete> crud = createGrid(crudFormFactory);		
		defineFilters(crud);
		defineQueries(crud);
		fillHW(crud, this);
	}

	protected void defineQueries(GridCrud<Athlete> crud) {
		crud.setFindAllOperation(
			DataProvider.fromCallbacks(
				query -> AthleteRepository
					.findFiltered(lastNameFilter.getValue(), groupFilter.getValue(), categoryFilter.getValue(),
						ageDivisionFilter.getValue(), null, query.getOffset(), query.getLimit())
					.stream(),
				query -> AthleteRepository.countFiltered(lastNameFilter.getValue(), groupFilter.getValue(),
					categoryFilter.getValue(), ageDivisionFilter.getValue(), null)));
	}

	protected GridCrud<Athlete> createGrid(OwlcmsCrudFormFactory<Athlete> crudFormFactory) {
		Grid<Athlete> grid = new Grid<Athlete>(Athlete.class, false);
		grid.addColumn("lastName").setHeader("Last Name");
		grid.addColumn("firstName").setHeader("First Name");
		grid.addColumn("team").setHeader("Team");
		grid.addColumn("ageDivision").setHeader("Age Division");
		grid.addColumn("category").setHeader("Category");
		grid.addColumn(new NumberRenderer<Athlete>(Athlete::getBodyWeight, "%.2f", this.getLocale())).setHeader("Body Weight");
		grid.addColumn("group").setHeader("Group");
		grid.addColumn("invited").setHeader("Invited");	
		GridCrud<Athlete> crud = new OwlcmsGridCrud<Athlete>(Athlete.class,
				new OwlcmsCrudLayout(Athlete.class),
				crudFormFactory,
				grid);
		crud.setCrudListener(this);
		crud.setClickRowToUpdate(true);
		return crud;
	}

	/**
	 * Define the form used to edit a given athlete
	 * 
	 * @return the form factory that will create the actual form on demand
	 */
	protected OwlcmsCrudFormFactory<Athlete> createFormFactory() {
		OwlcmsCrudFormFactory<Athlete> crudFormFactory = new OwlcmsCrudFormFactory<Athlete>(Athlete.class) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			protected void bindField(HasValue field, String property, Class<?> propertyType) {
				Binder.BindingBuilder bindingBuilder = binder.forField(field);

				if (field instanceof Bindable) {
					bindingBuilder.withConverter(((Bindable) field).getConverter());

					if ("fullBirthDate".equals(property)) {
						fullBirthDateValidation(bindingBuilder);
						bindingBuilder.bind(property);
					} else if ("bodyWeight".equals(property)) {
						bodyWeightValidation(bindingBuilder);
						bindingBuilder.bind(property);
					} else {
						throw new RuntimeException("property " + property + " is Bindable but not covered.");
					}
				} else if ("category".equals(property)) {
					categoryValidation(bindingBuilder);
					bindingBuilder.bind(property);
				} else {
					super.bindField(field, property, propertyType);
				}
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			protected void fullBirthDateValidation(Binder.BindingBuilder bindingBuilder) {
				Validator<LocalDate> v = Validator.from(
					ld -> (ld.compareTo(LocalDate.now()) <= 0),
					"cannot be in the future");
				bindingBuilder.withValidator(v);
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			protected void bodyWeightValidation(Binder.BindingBuilder bindingBuilder) {
				Validator<Double> v1 = new DoubleRangeValidator(
						"Weight should be between 0 and 350kg", 0.0D, 350.0D);
				// check wrt body category
				Validator<Double> v2 = Validator
					.from((weight) -> {
						Binding<Athlete, ?> categoryBinding = binder.getBinding("category").get();
						// we signal the error on the category drop down
						categoryBinding.validate(true).isError();
						return true;
					}, "Body Weight is outside of selected category");
				bindingBuilder.withValidator(v2);
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			protected void categoryValidation(Binder.BindingBuilder bindingBuilder) {
				// check wrt body weight
				Validator<Category> v = Validator
					.from((category) -> {
						try {
							Binding<Athlete, ?> bwBinding = binder.getBinding("bodyWeight").get();
							String bwString = (String) bwBinding.getField().getValue();
							if (bwString == null) {
								// no body weight - no contradiction
								return true;
							}
							Double bw = Double.parseDouble(bwString);
							Double min = category.getMinimumWeight();
							Double max = category.getMaximumWeight();
							logger.debug(
								"comparing {} ]{},{}] with body weight {}",category.getName(),  min,  max, bw);
							return (bw > min && bw <= max);
						} catch (Exception e) {
							e.printStackTrace();
						}
						return true;
					},
						"Category does not match body weight");
				bindingBuilder.withValidator(v);
			}
		};
		crudFormFactory.setVisibleProperties("lastName",
			"firstName",
			"gender",
			"fullBirthDate",
			"ageDivision",
			"category",
			"group",
			"bodyWeight",
			"snatch1Declaration",
			"cleanJerk1Declaration");
		crudFormFactory.setFieldCaptions("Last Name",
			"First Name",
			"Gender",
			"Birth Date",
			"Age Division",
			"Category",
			"Group",
			"Body Weight",
			"Snatch Declaration",
			"Clean&Jerk Declaration");
		crudFormFactory.setFieldProvider("gender",
            new ComboBoxProvider<>("Gender", Arrays.asList(Gender.values()), new TextRenderer<>(Gender::name), Gender::name));
		crudFormFactory.setFieldProvider("group",
            new ComboBoxProvider<>("Group", GroupRepository.findAll(), new TextRenderer<>(Group::getName), Group::getName));
		crudFormFactory.setFieldProvider("category",
            new ComboBoxProvider<>("Category", CategoryRepository.findAll(), new TextRenderer<>(Category::getName), Category::getName));
		crudFormFactory.setFieldProvider("ageDivision",
            new ComboBoxProvider<>("AgeDivision", Arrays.asList(AgeDivision.values()), new TextRenderer<>(AgeDivision::name), AgeDivision::name));
		
		crudFormFactory.setFieldType("bodyWeight", BodyWeightField.class);
		crudFormFactory.setFieldType("fullBirthDate", LocalDateField.class);
		
		return crudFormFactory;
	}

	protected void defineFilters(GridCrud<Athlete> crud) {
		lastNameFilter.setPlaceholder("Last name");
		lastNameFilter.setClearButtonVisible(true);
		lastNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
		lastNameFilter.addValueChangeListener(e -> {
			crud.refreshGrid();
		});
		crud.getCrudLayout()
			.addFilterComponent(lastNameFilter);

		ageDivisionFilter.setPlaceholder("Age Division");
		ageDivisionFilter.setItems(AgeDivision.findAll());
		ageDivisionFilter.setItemLabelGenerator(AgeDivision::name);
		ageDivisionFilter.addValueChangeListener(e -> {
			crud.refreshGrid();
		});
		crud.getCrudLayout()
			.addFilterComponent(ageDivisionFilter);

		categoryFilter.setPlaceholder("Category");
		categoryFilter.setItems(CategoryRepository.findAll());
		categoryFilter.setItemLabelGenerator(Category::getName);
		categoryFilter.addValueChangeListener(e -> {
			crud.refreshGrid();
		});
		crud.getCrudLayout()
			.addFilterComponent(categoryFilter);
		
		groupFilter.setPlaceholder("Group");
		groupFilter.setItems(GroupRepository.findAll());
		groupFilter.setItemLabelGenerator(Group::getName);
		groupFilter.addValueChangeListener(e -> {
			crud.refreshGrid();
		});
		crud.getCrudLayout()
			.addFilterComponent(groupFilter);
		
		weighedInFilter.addValueChangeListener(e -> {
			crud.refreshGrid();
		});
		weighedInFilter.setLabel("Weighed-in");
		crud.getCrudLayout()
			.addFilterComponent(weighedInFilter);
		
		Button clearFilters = new Button(null, VaadinIcon.ERASER.create());
		clearFilters.addClickListener(event -> {
			lastNameFilter.clear();
			ageDivisionFilter.clear();
			categoryFilter.clear();
			groupFilter.clear();
			weighedInFilter.clear();
		});
		crud.getCrudLayout()
			.addFilterComponent(clearFilters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vaadin.crudui.crud.CrudListener#add(java.lang.Object)
	 */
	@Override
	public Athlete add(Athlete Athlete) {
		AthleteRepository.save(Athlete);
		return Athlete;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vaadin.crudui.crud.CrudListener#update(java.lang.Object)
	 */
	@Override
	public Athlete update(Athlete Athlete) {
		if (Athlete.getId()
			.equals(5l)) {
			throw new RuntimeException("A simulated error has occurred");
		}
		return AthleteRepository.save(Athlete);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vaadin.crudui.crud.CrudListener#delete(java.lang.Object)
	 */
	@Override
	public void delete(Athlete Athlete) {
		AthleteRepository.delete(Athlete);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vaadin.crudui.crud.CrudListener#findAll()
	 */
	@Override
	public Collection<Athlete> findAll() {
		return AthleteRepository.findAll();
	}

}
