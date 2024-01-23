package com.aem.vertis.core.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Model(
    adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class MegaMenuModel {

    private static final Logger LOG = LoggerFactory.getLogger(MegaMenuModel.class);

    private List<MenuPage> menuPages;
    private String rootPath;

    public MegaMenuModel(SlingHttpServletRequest request) {
        // Retrieve the root path from the component's properties
        Resource resource = request.getResource();
        rootPath = resource.getValueMap().get("megaMenuPath", String.class);

        // Logging the root path
        LOG.info("MegaMenuModel initialized with rootPath: {}", rootPath);

        // Get the PageManager directly injected
        PageManager pageManager = request.getResource().getResourceResolver().adaptTo(PageManager.class);

        // Get the child pages under the specified root path
        Page rootPage = pageManager.getPage(rootPath);

        if (rootPage != null) {
            menuPages = new ArrayList<>();

            // Use PageIterator instead of Iterator
            Iterator<Page> childPages = rootPage.listChildren();

            while (childPages.hasNext()) {
                Page childPage = childPages.next();

                // Logging the child page details
                LOG.debug("Processing child page: {}", childPage.getPath());

                if (!getBooleanProperty(childPage, "hideInNavigation", false)) {
                    MenuPage menuPage = new MenuPage(childPage);
                    menuPages.add(menuPage);

                    if (!getBooleanProperty(childPage, "hideSubpagesInNavigation", false)) {
                        List<MenuPage> subMenuPages = new ArrayList<>();

                        Iterator<Page> subPages = childPage.listChildren();

                        while (subPages.hasNext()) {
                            Page subPage = subPages.next();

                            // Logging the sub page details
                            LOG.debug("Processing sub page: {}", subPage.getPath());

                            if (!getBooleanProperty(subPage, "hideInNavigation", false)) {
                                subMenuPages.add(new MenuPage(subPage));
                            }
                        }
                        menuPage.setSubMenuPages(subMenuPages);
                    }
                }
            }
        }
    }

    private boolean getBooleanProperty(Page page, String propertyName, boolean defaultValue) {
        // Use get(String, T) and handle the case when the value is null
        Boolean value = page.getProperties().get(propertyName, Boolean.class);
        return value != null ? value : defaultValue;
    }

    public List<MenuPage> getMenuPages() {
        return menuPages;
    }

    public String getRootPath() {
        return rootPath;
    }

    public static class MenuPage {
        private String title;
        private String path;
        private List<MenuPage> subMenuPages;

        public MenuPage(Page page) {
            this.title = page.getTitle();
            this.path = page.getPath();
        }

        public String getTitle() {
            return title;
        }

        public String getPath() {
            return path;
        }

        public List<MenuPage> getSubMenuPages() {
            return subMenuPages;
        }

        public void setSubMenuPages(List<MenuPage> subMenuPages) {
            this.subMenuPages = subMenuPages;
        }
    }
}
