package hu.infokristaly.archiwar.front.manager;

import hu.infokristaly.archiwar.back.domain.Clerk;
import hu.infokristaly.archiwar.back.domain.Usergroup;
import hu.infokristaly.archiwar.front.annotations.EntityInfo;
import hu.infokristaly.archiwar.middle.services.ClerkService;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.metadata.ConstraintDescriptor;

import org.apache.commons.beanutils.BeanUtils;
import org.primefaces.extensions.model.dynaform.DynaFormControl;
import org.primefaces.extensions.model.dynaform.DynaFormLabel;
import org.primefaces.extensions.model.dynaform.DynaFormModel;
import org.primefaces.extensions.model.dynaform.DynaFormRow;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@SessionScoped
public class UserManager implements Serializable {

	private static final long serialVersionUID = 3114254437517370726L;

    @Inject
    private Logger log;

	@Inject
	private ClerkService userService;

	private List<ColumnModel> columns;
	
    private List<Clerk> selectedUsers;
    
    private List<Usergroup> usergroup;

    private LazyDataModel<Clerk> lazyDataModel;
    
    private DynaFormModel formModel;

    public UserManager() {
    	
    }

    @PostConstruct
    public void init() {
        log.log(Level.INFO, "["+this.getClass().getName()+"] constructor finished.");
        Usergroup g1 = new Usergroup("admin");
        Usergroup g2 = new Usergroup("clerk");
        userService.persistGroup(g1);
        userService.persistGroup(g2);
        usergroup =  userService.findGroups();
        initModel();
    }

    private void initModel() {
        columns = new ArrayList<ColumnModel>();
        formModel = new DynaFormModel();
        DynaFormRow row = formModel.createRegularRow();
        FieldModel model = new FieldModel("photo","hidden",false);        
        DynaFormControl control = row.addControl(model,"hidden");
        
        Class<?> clazz = Clerk.class;
        
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(EntityInfo.class)) {
				EntityInfo entityInfo = field.getAnnotation(EntityInfo.class);
				columns.add(new ColumnModel(entityInfo.info(),field.getName()));
								
				row = formModel.createRegularRow();
				DynaFormLabel label = row.addLabel(entityInfo.info());
				FieldModel fmodel = new FieldModel(field.getName(), entityInfo.info(), entityInfo.required());
				if (!entityInfo.detailLabelfield().isEmpty()) {
				    fmodel.setDetailLabelfield(entityInfo.detailLabelfield());
				}
				control = row.addControl(fmodel,entityInfo.editor());				
				label.setForControl(control);
			}
		}
	}

	public LazyDataModel<Clerk> getLazyDataModel() {
        if (lazyDataModel == null) {
            lazyDataModel = new LazyDataModel<Clerk>() {

                private static final long serialVersionUID = 1678907483750487431L;

                private Map<String, Object> actualfilters;

                private String actualOrderField;
                private SortOrder actualSortOrder;
                private Integer count;

                @Override
                public Clerk getRowData(String rowKey) {
                    long primaryKey = Long.valueOf(rowKey);
                    
                    Clerk user = new Clerk(primaryKey);
                    user = userService.find(user);
                    return user;
                }

                @Override
                public Object getRowKey(Clerk user) {
                    return user.getId();
                }

                @Override
                public List<Clerk> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
                    this.setPageSize(pageSize);
                    this.count = null;
                    this.actualfilters = filters;
                    if (sortField != null) {
                        this.actualOrderField = sortField;
                    }
                    if (sortOrder != null) {
                        this.actualSortOrder = sortOrder;
                    }
                    List<Clerk> result = (List<Clerk>) userService.findRange(first, pageSize, this.actualOrderField, this.actualSortOrder, filters);
                    log.log(Level.INFO, "["+this.getClass().getName()+"] load finished.");
                    return result;
                }

                @Override
                public int getRowCount() {
                	if (count == null) {
                		count =  userService.count(actualfilters);
                	}
                    return count;
                }

            };
        }
        return lazyDataModel;
    }

	public List<Clerk> getSelectedUsers() {
		return selectedUsers;
	}

	public void setSelectedUsers(List<Clerk> selectedUsers) {
		this.selectedUsers = selectedUsers;
	}

    public List<ColumnModel> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnModel> columns) {
		this.columns = columns;
	}

	private Clerk value = new Clerk();

	public Clerk getValue() {
		return value;
	}

	public void setValue(Clerk user) {
		this.value = user;
	}

	public DynaFormModel getFormModel() {
		return formModel;
	}

	public void setFormModel(DynaFormModel formModel) {
		this.formModel = formModel;
	}

	public void initValue() {
		if (value.getId() != null) {
			value = new Clerk();
		}
	}

	public List<Clerk> getUserList() {
		return userService.findAll(true);
	}

    public List<FieldModel> getProperties() {  
        if (formModel == null) {  
            return null;  
        }  
  
        List<FieldModel> properties = new ArrayList<FieldModel>();  
        for (DynaFormControl dynaFormControl : formModel.getControls()) {  
            properties.add((FieldModel) dynaFormControl.getData());  
        }  
  
        return properties;  
    }
    
    public void clearProperties() {
        for (DynaFormControl dynaFormControl : formModel.getControls()) {  
            ((FieldModel)dynaFormControl.getData()).setValue(null);  
        }        
    }

	public void save() {
		List<FieldModel> fields = getProperties();
		
		fields.forEach(f -> {try {
			BeanUtils.setProperty(value, f.propertyName, f.value);
		} catch (Exception e) {
			e.printStackTrace();
		}});		
		try {
			
			if (value.getId() == null) {
				userService.persist(value);
			} else {
				userService.merge(value);
			}
			
			value = new Clerk();
			clearProperties();
            FacesMessage message = new FacesMessage("User saved.");
            FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (EJBException e) {
			Throwable ex = e.getCause();
			if (ex instanceof ConstraintViolationException) {
				Set<ConstraintViolation<?>> msg = ((ConstraintViolationException) ex).getConstraintViolations();
				msg.forEach(c -> {
					StringBuffer validationExcMsg = new StringBuffer();
					ConstraintDescriptor<?> desc = c.getConstraintDescriptor();
					System.out.println(desc.getAttributes().get("message"));
					String temp = c.getMessageTemplate();
					System.out.println(temp);
					validationExcMsg.append(c.getPropertyPath()).append(":").append(c.getMessage());
					FacesMessage message = new FacesMessage(validationExcMsg.toString());
					FacesContext.getCurrentInstance().addMessage(null, message);
				});
			} else {
				FacesMessage message = new FacesMessage("Failed: " + e.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, message);
			}
		}
		
	}
	
	public void deleteSelected() {		
		for(Clerk c: selectedUsers) {			
			userService.removeSystemUser(c);
		};
	}

    public List<Usergroup> getUsergroup() {
        return usergroup;
    }

    public void setUsergroup(List<Usergroup> usergroup) {
        this.usergroup = usergroup;
    }

    static public class ColumnModel implements Serializable {
    	 
		private static final long serialVersionUID = 4063465058747639784L;

		private String header;
        private String property;
 
        public ColumnModel(String header, String property) {
            this.header = header;
            this.property = property;
        }
 
        public String getHeader() {
            return header;
        }
 
        public String getProperty() {
            return property;
        }
    }

    static public class FieldModel implements Serializable {
   	 
		private static final long serialVersionUID = 4063465058747639784L;

		private String label;
		private String propertyName;
        private Object value;
        private boolean required;
        private String detailLabelfield;
 
        public FieldModel(String propertyName,String label, boolean required) {
        	this.propertyName = propertyName;
            this.label = label;
            this.required = required;
        }

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getPropertyName() {
			return propertyName;
		}

		public void setPropertyName(String propertyName) {
			this.propertyName = propertyName;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		public boolean isRequired() {
			return required;
		}

		public void setRequired(boolean required) {
			this.required = required;
		}

        public String getDetailLabelfield() {
            return detailLabelfield;
        }

        public void setDetailLabelfield(String detailLabelfield) {
            this.detailLabelfield = detailLabelfield;
        }
 
    }

}
