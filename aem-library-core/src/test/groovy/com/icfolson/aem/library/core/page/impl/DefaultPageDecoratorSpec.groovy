package com.icfolson.aem.library.core.page.impl

import com.day.cq.wcm.api.NameConstants
import com.google.common.base.Predicate
import com.google.common.base.Predicates
import com.icfolson.aem.library.api.node.BasicNode
import com.icfolson.aem.library.api.node.ComponentNode
import com.icfolson.aem.library.api.page.PageDecorator
import com.icfolson.aem.library.api.page.enums.TitleType
import com.icfolson.aem.library.core.page.predicates.TemplatePredicate
import com.icfolson.aem.library.core.specs.AemLibrarySpec
import spock.lang.Unroll

@Unroll
class DefaultPageDecoratorSpec extends AemLibrarySpec {

    def setupSpec() {
        pageBuilder.content {
            citytechinc("CITYTECH, Inc.") {
                "jcr:content"(otherPagePath: "/content/ales/esb", pageTitle: "Page Title",
                    navTitle: "Navigation Title") {
                    component {
                        one("sling:resourceType": "one")
                        two("sling:resourceType": "two")
                    }
                }
                child1 {
                    "jcr:content"(hideInNav: true, "cq:template": "template")
                    grandchild()
                }
                child2 {
                    "jcr:content"(pageTitle: "Child 2", "jcr:title": "Also Child 2") {
                        image(fileReference: "/content/dam/image")
                        secondimage(fileReference: "/content/dam/image")
                    }
                }
            }
            other()
            inheritance {
                "jcr:content"("jcr:title": "Inheritance") {
                    component("jcr:title": "Component", "number": 5, "boolean": false) {
                        image(fileReference: "/content/dam/image")
                        secondimage(fileReference: "/content/dam/image")
                    }
                }
                child {
                    "jcr:content" {
                        component()
                        other()
                    }
                    sub()
                }
            }
        }

        nodeBuilder.content {
            citytechinc { empty(NameConstants.NT_PAGE) }
            dam("sling:Folder") {
                image("dam:Asset") {
                    "jcr:content" {
                        renditions("nt:folder") {
                            original("nt:file") {
                                "jcr:content"("nt:resource", "jcr:data": "data")
                            }
                        }
                    }
                }
            }
        }
    }

    def "get absolute parent"() {
        setup:
        def page = getPageDecorator("/content/citytechinc/child1")

        expect:
        page.getAbsoluteParent(level).path == absoluteParentPath

        where:
        level | absoluteParentPath
        0     | "/content"
        1     | "/content/citytechinc"
        2     | "/content/citytechinc/child1"
    }

    def "get parent"() {
        setup:
        def page = getPageDecorator("/content/citytechinc/child1")

        expect:
        page.getParent(level).path == parentPath

        where:
        level | parentPath
        2     | "/content"
        1     | "/content/citytechinc"
        0     | "/content/citytechinc/child1"
    }

    def "adapt to"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")

        expect:
        page.adaptTo(type).path == "/content/citytechinc/jcr:content"

        where:
        type << [BasicNode, ComponentNode]
    }

    def "adapt to returns null"() {
        setup:
        def page = getPageDecorator("/content/citytechinc/empty")

        expect:
        !page.adaptTo(type)

        where:
        type << [BasicNode, ComponentNode]
    }

    def "as map"() {
        setup:
        def page = getPageDecorator(path)

        expect:
        page.asMap().containsKey(propertyName) == result

        where:
        path                         | propertyName          | result
        "/content/citytechinc"       | "otherPagePath"       | true
        "/content/citytechinc"       | "nonExistentProperty" | false
        "/content/citytechinc/empty" | "otherPagePath"       | false
    }

    def "get"() {
        setup:
        def page = getPageDecorator(path)

        expect:
        page.get(propertyName, defaultValue) == result

        where:
        path                         | propertyName          | defaultValue | result
        "/content/citytechinc"       | "otherPagePath"       | ""           | "/content/ales/esb"
        "/content/citytechinc"       | "nonExistentProperty" | ""           | ""
        "/content/citytechinc/empty" | "otherPagePath"       | "/content"   | "/content"
    }

    def "get optional"() {
        setup:
        def page = getPageDecorator(path)

        expect:
        page.get(propertyName, String).present == result

        where:
        path                         | propertyName          | result
        "/content/citytechinc"       | "otherPagePath"       | true
        "/content/citytechinc"       | "nonExistentProperty" | false
        "/content/citytechinc/empty" | "otherPagePath"       | false
    }

    def "get as href"() {
        setup:
        def page = getPageDecorator(path)

        expect:
        page.getAsHref(propertyName).present == result

        where:
        path                         | propertyName          | result
        "/content/citytechinc"       | "otherPagePath"       | true
        "/content/citytechinc"       | "nonExistentProperty" | false
        "/content/citytechinc/empty" | "otherPagePath"       | false
    }

    def "get as href strict"() {
        setup:
        def page = getPageDecorator(path)

        expect:
        page.getAsHref(propertyName, true).present == result

        where:
        path                         | propertyName          | result
        "/content/citytechinc"       | "otherPagePath"       | true
        "/content/citytechinc"       | "nonExistentProperty" | false
        "/content/citytechinc/empty" | "otherPagePath"       | false
    }

    def "get as mapped href"() {
        setup:
        def page = getPageDecorator(path)

        expect:
        page.getAsHref(propertyName, false, true).present == result

        where:
        path                         | propertyName          | result
        "/content/citytechinc"       | "otherPagePath"       | true
        "/content/citytechinc"       | "nonExistentProperty" | false
        "/content/citytechinc/empty" | "otherPagePath"       | false
    }

    def "get as mapped href strict"() {
        setup:
        def page = getPageDecorator(path)

        expect:
        page.getAsHref(propertyName, true, true).present == result

        where:
        path                         | propertyName          | result
        "/content/citytechinc"       | "otherPagePath"       | true
        "/content/citytechinc"       | "nonExistentProperty" | false
        "/content/citytechinc/empty" | "otherPagePath"       | false
    }

    def "get image source optional"() {
        setup:
        def page = getPageDecorator(path)

        expect:
        page.imageSource.present == isPresent

        where:
        path                          | isPresent
        "/content/citytechinc/child1" | false
        "/content/citytechinc/child2" | true
    }

    def "get image source"() {
        setup:
        def page = getPageDecorator("/content/citytechinc/child2")

        expect:
        page.imageSource.get() == "/content/citytechinc/child2.img.png"
    }

    def "get named image source"() {
        setup:
        def page = getPageDecorator("/content/citytechinc/child2")

        expect:
        page.getImageSource(name).get() == imageSource

        where:
        name          | imageSource
        "image"       | "/content/citytechinc/child2.img.png"
        "secondimage" | "/content/citytechinc/child2.img.secondimage.png"
    }

    def "get image source with width"() {
        setup:
        def page = getPageDecorator("/content/citytechinc/child2")

        expect:
        page.getImageSource(width).get() == imageSource

        where:
        width | imageSource
        -1    | "/content/citytechinc/child2.img.png"
        100   | "/content/citytechinc/child2.img.100.png"
    }

    def "get named image source with width"() {
        setup:
        def page = getPageDecorator("/content/citytechinc/child2")

        expect:
        page.getImageSource(name, width).get() == imageSource

        where:
        name          | width | imageSource
        "image"       | -1    | "/content/citytechinc/child2.img.png"
        "image"       | 100   | "/content/citytechinc/child2.img.100.png"
        "secondimage" | -1    | "/content/citytechinc/child2.img.secondimage.png"
        "secondimage" | 100   | "/content/citytechinc/child2.img.secondimage.100.png"
    }

    def "find ancestor optional"() {
        setup:
        def page = getPageDecorator(path)
        def predicate = new Predicate<PageDecorator>() {
            @Override
            boolean apply(PageDecorator input) {
                input.title == "CITYTECH, Inc."
            }
        }

        expect:
        page.findAncestor(predicate, excludeCurrentResource).present == isPresent

        where:
        path                          | excludeCurrentResource | isPresent
        "/content/citytechinc"        | false                  | true
        "/content/citytechinc/child1" | false                  | true
        "/content/other"              | false                  | false
        "/content/citytechinc"        | true                   | false
        "/content/citytechinc/child1" | true                   | true
        "/content/other"              | true                   | false
    }

    def "find ancestor with property"() {
        setup:
        def page = getPageDecorator(path)
        def ancestorPageOptional = page.findAncestorWithProperty("jcr:title", excludeCurrentResource)

        expect:
        ancestorPageOptional.get().path == ancestorPath

        where:
        path                             | excludeCurrentResource | ancestorPath
        "/content/inheritance"           | false                  | "/content/inheritance"
        "/content/inheritance/child"     | false                  | "/content/inheritance"
        "/content/inheritance/child/sub" | false                  | "/content/inheritance"
        "/content/inheritance/child"     | true                   | "/content/inheritance"
        "/content/inheritance/child/sub" | true                   | "/content/inheritance"
    }

    def "find ancestor returns absent"() {
        setup:
        def page = getPageDecorator(path)
        def ancestorPageOptional = page.findAncestorWithProperty("jcr:description", excludeCurrentResource)

        expect:
        !ancestorPageOptional.present

        where:
        path                             | excludeCurrentResource
        "/content/inheritance"           | false
        "/content/inheritance/child"     | false
        "/content/inheritance/child/sub" | false
        "/content/inheritance"           | true
        "/content/inheritance/child"     | true
        "/content/inheritance/child/sub" | true
    }

    def "find ancestor with property value"() {
        setup:
        def page = getPageDecorator("/content/inheritance/child/sub")

        expect:
        page.findAncestorWithPropertyValue("jcr:title", "Inheritance").get().path == "/content/inheritance"

        and:
        page.findAncestorWithPropertyValue("jcr:title", "Inheritance", true).get().path == "/content/inheritance"
    }

    def "find ancestor with property value returns absent"() {
        setup:
        def page = getPageDecorator("/content/inheritance/child/sub")
        def ancestorPageOptional = page.findAncestorWithPropertyValue("jcr:title", "Foo")

        expect:
        !ancestorPageOptional.present
    }

    def "get template path"() {
        setup:
        def page = getPageDecorator(path)

        expect:
        page.templatePath == templatePath

        where:
        path                          | templatePath
        "/content/citytechinc"        | ""
        "/content/citytechinc/child1" | "template"
    }

    def "get component node"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")

        expect:
        page.componentNode.get().path == "/content/citytechinc/jcr:content"
    }

    def "get component node returns absent optional for page with no jcr:content node"() {
        setup:
        def page = getPageDecorator("/content/citytechinc/empty")

        expect:
        !page.componentNode.present
    }

    def "get component node at relative path"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")

        expect:
        page.getComponentNode("component/one").get().path == "/content/citytechinc/jcr:content/component/one"
    }

    def "adapt to basic node"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")

        expect:
        page.adaptTo(BasicNode).path == "/content/citytechinc/jcr:content"
    }

    def "adapt to component node"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")

        expect:
        page.adaptTo(ComponentNode).path == "/content/citytechinc/jcr:content"
    }

    def "adapt to node for page with no jcr:content node returns null"() {
        setup:
        def page = getPageDecorator("/content/citytechinc/empty")

        expect:
        !page.adaptTo(ComponentNode)
    }

    def "get child"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")

        expect:
        page.getChild("child2").get().title == "Also Child 2"
    }

    def "get children"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")

        expect:
        page.children.size() == 3
    }

    def "list children"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")

        expect:
        page.listChildPages().size() == 3
    }

    def "get displayable children"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")

        expect:
        page.getChildren(true).size() == 1
    }

    def "get children filtered for predicate"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")
        def predicate = new TemplatePredicate("template")

        expect:
        page.getChildren(predicate).size() == 1
    }

    def "list children filtered for predicate"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")
        def predicate = new TemplatePredicate("template")

        expect:
        page.listChildPages(predicate).size() == 1
    }

    def "list children recursively, filtered for predicate"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")

        expect:
        page.listChildPages(predicate, true).size() == size

        where:
        predicate                         | size
        new TemplatePredicate("template") | 1
        Predicates.alwaysTrue()           | 4
    }

    def "find descendants"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")
        def predicate = Predicates.alwaysTrue()

        expect:
        page.findDescendants(predicate).size() == 4
    }

    def "get properties"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")

        expect:
        page.properties.keySet().containsAll(["jcr:title", "otherPagePath"])
    }

    def "get properties for page with no jcr:content node"() {
        setup:
        def page = getPageDecorator("/content/citytechinc/empty")

        expect:
        page.properties.isEmpty()
    }

    def "get properties at relative path"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")

        expect:
        page.getProperties("component/one").containsKey("sling:resourceType")
    }

    def "get title"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")

        expect:
        page.getTitle(titleType).get() == title

        where:
        titleType                  | title
        TitleType.TITLE            | "CITYTECH, Inc."
        TitleType.NAVIGATION_TITLE | "Navigation Title"
        TitleType.PAGE_TITLE       | "Page Title"
    }

    def "get title returns absent where appropriate"() {
        setup:
        def page = getPageDecorator("/content/citytechinc/child1")

        expect:
        !page.getTitle(titleType).present

        where:
        titleType << [
            TitleType.NAVIGATION_TITLE,
            TitleType.PAGE_TITLE
        ]
    }

    def "get image link"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")
        def imageLink = page.getImageLink("/image")

        expect:
        imageLink.href == "/content/citytechinc.html"
        imageLink.imageSource == "/image"
    }

    def "get navigation link"() {
        setup:
        def page = getPageDecorator("/content/citytechinc")
        def navigationLink = page.navigationLink

        expect:
        navigationLink.href == "/content/citytechinc.html"
        navigationLink.title == "Navigation Title"
    }
}
