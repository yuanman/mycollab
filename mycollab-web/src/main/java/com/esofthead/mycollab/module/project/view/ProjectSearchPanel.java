/**
 * This file is part of mycollab-web.
 *
 * mycollab-web is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mycollab-web is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mycollab-web.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.esofthead.mycollab.module.project.view;

import com.esofthead.mycollab.common.i18n.GenericI18Enum;
import com.esofthead.mycollab.core.arguments.StringSearchField;
import com.esofthead.mycollab.core.db.query.Param;
import com.esofthead.mycollab.module.project.ProjectTypeConstants;
import com.esofthead.mycollab.module.project.domain.criteria.ProjectSearchCriteria;
import com.esofthead.mycollab.module.project.i18n.ProjectI18nEnum;
import com.esofthead.mycollab.module.project.ui.components.ComponentUtils;
import com.esofthead.mycollab.security.RolePermissionCollections;
import com.esofthead.mycollab.vaadin.AppContext;
import com.esofthead.mycollab.vaadin.mvp.ViewManager;
import com.esofthead.mycollab.vaadin.web.ui.DefaultGenericSearchPanel;
import com.esofthead.mycollab.vaadin.web.ui.DynamicQueryParamLayout;
import com.esofthead.mycollab.vaadin.web.ui.ShortcutExtension;
import com.esofthead.mycollab.vaadin.web.ui.UIConstants;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.layouts.MHorizontalLayout;

/**
 * @author MyCollab Ltd
 * @since 5.2.12
 */
public class ProjectSearchPanel extends DefaultGenericSearchPanel<ProjectSearchCriteria> {
    private static Param[] paramFields = new Param[]{
            ProjectSearchCriteria.p_name, ProjectSearchCriteria.p_status, ProjectSearchCriteria.p_startdate,
            ProjectSearchCriteria.p_enddate, ProjectSearchCriteria.p_createdtime
    };

    @Override
    protected SearchLayout<ProjectSearchCriteria> createBasicSearchLayout() {
        return new ProjectBasicSearchLayout();
    }

    @Override
    protected SearchLayout<ProjectSearchCriteria> createAdvancedSearchLayout() {
        return new ProjectAdvancedSearchLayout();
    }

    @Override
    protected ComponentContainer buildSearchTitle() {
        return ComponentUtils.headerH2(ProjectTypeConstants.PROJECT, AppContext.getMessage(ProjectI18nEnum.LIST));
    }

    @Override
    protected Component buildExtraControls() {
        MButton createBtn = new MButton(AppContext.getMessage(ProjectI18nEnum.NEW),
                clickEvent -> UI.getCurrent().addWindow(ViewManager.getCacheComponent(AbstractProjectAddWindow.class)))
                .withStyleName(UIConstants.BUTTON_ACTION).withIcon(FontAwesome.PLUS);
        createBtn.setEnabled(AppContext.canBeYes(RolePermissionCollections.CREATE_NEW_PROJECT));
        return createBtn;
    }

    private class ProjectBasicSearchLayout extends BasicSearchLayout<ProjectSearchCriteria> {
        private static final long serialVersionUID = 1L;

        private TextField nameField;

        public ProjectBasicSearchLayout() {
            super(ProjectSearchPanel.this);
        }

        @Override
        public ComponentContainer constructBody() {
            MHorizontalLayout basicSearchBody = new MHorizontalLayout().withMargin(true);

            nameField = ShortcutExtension.installShortcutAction(new TextField(),
                    new ShortcutListener("ProjectSearchRequest", ShortcutAction.KeyCode.ENTER, null) {
                        @Override
                        public void handleAction(Object o, Object o1) {
                            callSearchAction();
                        }
                    });
            nameField.setInputPrompt(AppContext.getMessage(ProjectI18nEnum.ACTION_QUERY_BY_PROJECT_NAME));
            nameField.setWidth(UIConstants.DEFAULT_CONTROL_WIDTH);
            basicSearchBody.with(nameField).withAlign(nameField, Alignment.MIDDLE_CENTER);

            MButton searchBtn = new MButton(AppContext.getMessage(GenericI18Enum.BUTTON_SEARCH), clickEvent ->
                    callSearchAction()).withIcon(FontAwesome.SEARCH).withStyleName(UIConstants.BUTTON_ACTION);
            basicSearchBody.with(searchBtn).withAlign(searchBtn, Alignment.MIDDLE_LEFT);

            MButton cancelBtn = new MButton(AppContext.getMessage(GenericI18Enum.BUTTON_CLEAR),
                    clickEvent -> nameField.setValue("")).withStyleName(UIConstants.BUTTON_OPTION);
            basicSearchBody.with(cancelBtn).withAlign(cancelBtn, Alignment.MIDDLE_CENTER);

            MButton advancedSearchBtn = new MButton(AppContext.getMessage(GenericI18Enum.BUTTON_ADVANCED_SEARCH),
                    clickEvent -> moveToAdvancedSearchLayout()).withStyleName(UIConstants.BUTTON_LINK);
            basicSearchBody.with(advancedSearchBtn).withAlign(advancedSearchBtn, Alignment.MIDDLE_CENTER);

            return basicSearchBody;
        }

        @Override
        protected ProjectSearchCriteria fillUpSearchCriteria() {
            ProjectSearchCriteria searchCriteria = new ProjectSearchCriteria();
            searchCriteria.setProjectName(StringSearchField.and(this.nameField.getValue().trim()));
            return searchCriteria;
        }

        @Override
        public ComponentContainer constructHeader() {
            return ProjectSearchPanel.this.constructHeader();
        }
    }

    private class ProjectAdvancedSearchLayout extends DynamicQueryParamLayout<ProjectSearchCriteria> {
        private static final long serialVersionUID = 1L;

        public ProjectAdvancedSearchLayout() {
            super(ProjectSearchPanel.this, ProjectTypeConstants.PROJECT);
        }

        @Override
        public ComponentContainer constructHeader() {
            return ProjectSearchPanel.this.constructHeader();
        }

        @Override
        public Param[] getParamFields() {
            return paramFields;
        }

        @Override
        protected Class<ProjectSearchCriteria> getType() {
            return ProjectSearchCriteria.class;
        }

        @Override
        protected Component buildSelectionComp(String fieldId) {
            return null;
        }
    }
}
