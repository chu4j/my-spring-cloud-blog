package org.zhuqigong.blogservice.util;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.util.HeadingCollectingVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.renderer.HeaderIdGenerator;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.PegdownExtensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.zhuqigong.blogservice.model.Category;
import org.zhuqigong.blogservice.model.Post;
import org.zhuqigong.blogservice.model.Tag;
import org.zhuqigong.blogservice.model.YamlMarkdownHeader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class MarkdownUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MarkdownUtil.class);
    private static final String UNKNOWN = "UNKNOWN";

    private MarkdownUtil() {
    }

    public static String formatMarkdown2Html(String markdown) {
        return formatMarkdown2Html(markdown, true);
    }

    public static String formatMarkdown2Html(String markdown, boolean containsYamlBlock) {
        DataHolder options =
                PegdownOptionsAdapter.flexmarkOptions(true, PegdownExtensions.ALL)
                        .toMutable()
                        .set(Parser.EXTENSIONS,
                                Collections.singleton(YamlFrontMatterExtension.create()))
                        .set(HtmlRenderer.RENDER_HEADER_ID, true)
                        .set(HtmlRenderer.SOFT_BREAK, "<br>")
                        .set(HtmlRenderer.GENERATE_HEADER_ID, true);
        Parser markdownParser = Parser.builder(options).build();
        HtmlRenderer htmlRenderer = HtmlRenderer.builder(options).build();
        Node node = markdownParser.parse("");
        Node nodes = markdownParser.parse(markdown);
        if (!containsYamlBlock) {
            nodes.getChildIterator().forEachRemaining(block -> {
                if (!(block instanceof YamlFrontMatterBlock)) {
                    node.appendChild(block);
                }
            });
            return htmlRenderer.render(node);
        } else {
            return htmlRenderer.render(nodes);
        }
    }

    public static Post format(String markdownTitle, String markdownText) {
        DataHolder options =
                PegdownOptionsAdapter.flexmarkOptions(true, PegdownExtensions.ALL)
                        .toMutable()
                        .set(Parser.EXTENSIONS, Collections.singleton(YamlFrontMatterExtension.create()))
                        .set(HtmlRenderer.RENDER_HEADER_ID, true)
                        .set(HtmlRenderer.SOFT_BREAK, "<br>")
                        .set(HtmlRenderer.GENERATE_HEADER_ID, true)
                        .set(HtmlRenderer.FENCED_CODE_LANGUAGE_CLASS_PREFIX, "");
        Parser markdownParser = Parser.builder(options).build();
        HtmlRenderer htmlRenderer = HtmlRenderer.builder(options).build();
        Node nodes = markdownParser.parse(markdownText);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final String defaultTitle = sdf.format(System.currentTimeMillis());
        YamlMarkdownHeader yamlMarkdownHeader = new YamlMarkdownHeader(defaultTitle, "konc", new Date(), Collections.singletonList(UNKNOWN), Collections.singletonList(UNKNOWN));
        Node markdownBody = markdownParser.parse("");
        AtomicBoolean hasYamlBlock = new AtomicBoolean(false);
        nodes.getChildIterator().forEachRemaining(block -> {
            if (block instanceof YamlFrontMatterBlock) {
                AbstractYamlFrontMatterVisitor visitor = new AbstractYamlFrontMatterVisitor();
                visitor.visit(block);
                Map<String, List<String>> data = visitor.getData();
                String title = data.getOrDefault("title", Collections.singletonList(markdownTitle == null || markdownTitle.isEmpty() ? defaultTitle : markdownTitle)).get(0);
                String author = data.getOrDefault("author", Collections.singletonList("Anonymous")).get(0);
                String dateStr = data.getOrDefault("date", Collections.singletonList(sdf.format(new Date()))).get(0);
                String backgroundImage = data.getOrDefault("background", Collections.singletonList(null)).get(0);
                Date date = null;
                try {
                    date = sdf.parse(dateStr);
                } catch (ParseException e) {
                    LOG.error("date parse exception:{}", e.getLocalizedMessage());
                }
                List<String> categories = data.getOrDefault("categories", Collections.singletonList(UNKNOWN));
                List<String> tags = data.getOrDefault("tags", Collections.singletonList(UNKNOWN));
                LOG.info("Title:{},\tAuthor:{},\tDate:{},\tCategories:{},\tTags:{}. ", title, author, date, Arrays.toString(categories.toArray()), Arrays.toString(tags.toArray()));
                yamlMarkdownHeader.setBackgroundImage(backgroundImage);
                yamlMarkdownHeader.setTitle(title);
                yamlMarkdownHeader.setAuthor(author);
                yamlMarkdownHeader.setDate(date);
                yamlMarkdownHeader.setCategories(categories);
                yamlMarkdownHeader.setTags(tags);
                hasYamlBlock.set(true);
            }
            markdownBody.appendChild(block);
        });
        if (!hasYamlBlock.get()) {
            yamlMarkdownHeader.setTitle(null == markdownTitle ? defaultTitle : markdownTitle);
        }
        HeadingCollectingVisitor headingVisitor = new HeadingCollectingVisitor();
        headingVisitor.collect(markdownBody);
        List<Heading> headingList = headingVisitor.getHeadings();
        int minLevel = headingList.stream().map(Heading::getLevel).min(Comparator.comparing(Integer::intValue)).orElse(1);
        List<Pair<String, String>> anchorRefs = headingList.stream()
                .filter(x -> x.getLevel() == minLevel)
                .map(x -> Pair.of(HeaderIdGenerator.generateId(x.getAnchorRefText(), " -_", false, true), x.getAnchorRefText()))
                .collect(Collectors.toList());
        String catalogue = anchorRefs.stream()
                .map(Pair::getSecond)
                .reduce((x1, x2) -> x1 + "$$" + x2)
                .orElse(null);
        String catalogueBody = "<ul>" + anchorRefs.stream()
                .map(pair -> String.format("<li><a href=\"#%s\">", pair.getFirst()) + pair.getSecond() + "</a></li>")
                .collect(Collectors.joining()) + "</ul>";
        Post post = new Post();
        String html = htmlRenderer.render(markdownBody);
        post.setTitle(yamlMarkdownHeader.getTitle());
        post.setAuthor(yamlMarkdownHeader.getAuthor());
        post.setPublishTime(yamlMarkdownHeader.getDate());
        post.setRemark1(yamlMarkdownHeader.getBackgroundImage());
        post.setCatalogue(catalogue);
        post.setCatalogueBody(catalogueBody);
        post.setCategories(yamlMarkdownHeader.getCategories().stream().map(Category::new).collect(Collectors.toList()));
        post.setTags(yamlMarkdownHeader.getTags().stream().map(Tag::new).collect(Collectors.toList()));
        post.setContent(markdownBody.getChildChars().toStringOrNull());
        post.setContentBody(html);
        return post;
    }
}
