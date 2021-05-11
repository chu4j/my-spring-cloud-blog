package org.zhuqigong.blogservice.task;

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
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.zhuqigong.blogservice.model.Category;
import org.zhuqigong.blogservice.model.Post;
import org.zhuqigong.blogservice.model.Tag;
import org.zhuqigong.blogservice.model.YamlMarkdownHeader;
import org.zhuqigong.blogservice.service.PostService;

@Component
@ConfigurationProperties(prefix = "cosine")
public class MarkdownFileLoaderTask {
  private static final Logger LOG = LoggerFactory.getLogger(MarkdownFileLoaderTask.class);
  private static final String UNKNOWN="UNKNOWN";
  private final PostService postService;
  @Value("${my.blog.markdown-file.dir}")
  private String path;

  public MarkdownFileLoaderTask(PostService postService) {
    this.postService = postService;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void loadingMarkdownStartUp() throws IOException {
    List<File> mdFiles = Files.walk(Paths.get(path), 8)
        .map(Path::toFile)
        .filter(file -> file.getName().endsWith(".md"))
        .collect(Collectors.toList());
    HashMap<String, String> codeNameHashMap = new HashMap<>();
    DataHolder options =
        PegdownOptionsAdapter.flexmarkOptions(true, PegdownExtensions.ALL)
            .toMutable()
            .set(Parser.EXTENSIONS,
                Collections.singleton(YamlFrontMatterExtension.create()))
            .set(HtmlRenderer.RENDER_HEADER_ID, true)
            .set(HtmlRenderer.SOFT_BREAK, "<br>")
            .set(HtmlRenderer.GENERATE_HEADER_ID, true)
            .set(HtmlRenderer.FENCED_CODE_LANGUAGE_CLASS_PREFIX, "")
            .set(HtmlRenderer.FENCED_CODE_LANGUAGE_CLASS_MAP, codeNameHashMap);
    Parser markdownParser = Parser.builder(options).build();
    HtmlRenderer htmlRenderer = HtmlRenderer.builder(options).build();
    for (File file : mdFiles) {
      String str = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
      Node nodes = markdownParser.parse(str);
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
      YamlMarkdownHeader yamlMarkdownHeader =
          new YamlMarkdownHeader(file.getName().replace(".md", ""), "konc", new Date(),
              Collections.singletonList(UNKNOWN), Collections.singletonList(UNKNOWN));
      Node markdownBody = markdownParser.parse("");
      nodes.getChildIterator().forEachRemaining(block -> {
        if (block instanceof YamlFrontMatterBlock) {
          AbstractYamlFrontMatterVisitor visitor = new AbstractYamlFrontMatterVisitor();
          visitor.visit(block);
          Map<String, List<String>> data = visitor.getData();
          String title =
              data.getOrDefault("title",
                  Collections.singletonList(file.getName().replace(".md", ""))).get(0);
          String author =
              data.getOrDefault("author", Collections.singletonList("Anonymous")).get(0);
          String dateStr =
              data.getOrDefault("date", Collections.singletonList(sdf.format(new Date()))).get(0);
          Date date = null;
          try {
            date = sdf.parse(dateStr);
          } catch (ParseException e) {
            LOG.error("date parse exception:{}", e.getLocalizedMessage());
          }
          List<String> categories =
              data.getOrDefault("categories", Collections.singletonList(UNKNOWN));
          List<String> tags = data.getOrDefault("tags", Collections.singletonList(UNKNOWN));
          LOG.info("File:{} ,Header info below:", file.getAbsoluteFile());
          LOG.info("Title:{},\tAuthor:{},\tDate:{},\tCategories:{},\tTags:{}. ", title, author,
              date, Arrays.toString(categories.toArray()), Arrays.toString(tags.toArray()));
          yamlMarkdownHeader.setTitle(title);
          yamlMarkdownHeader.setAuthor(author);
          yamlMarkdownHeader.setDate(date);
          yamlMarkdownHeader.setCategories(categories);
          yamlMarkdownHeader.setTags(tags);
        } else {
          markdownBody.appendChild(block);
        }
      });
      HeadingCollectingVisitor headingVisitor = new HeadingCollectingVisitor();
      headingVisitor.collect(markdownBody);
      List<Heading> headingList = headingVisitor.getHeadings();
      int minLevel =
          headingList.stream().map(Heading::getLevel).min(Comparator.comparing(Integer::intValue))
              .orElse(1);
      List<Pair<String, String>> anchorRefs =
          headingList.stream().filter(x -> x.getLevel() == minLevel)
              .map(x -> Pair
                  .of(HeaderIdGenerator.generateId(x.getAnchorRefText(), " -_", false, true),
                      x.getAnchorRefText()))
              .collect(Collectors.toList());
      String catalogue =
          anchorRefs.stream().map(Pair::getSecond).reduce((x1, x2) -> x1 + "$$" + x2).orElse(null);
      String catalogueBody = "<ul>" + anchorRefs.stream().map(
          pair -> String.format("<li><a href=\"#%s\">", pair.getFirst()) + pair.getSecond() +
              "</a></li>").collect(Collectors.joining()) + "</ul>";
      Post newPost = new Post();
      String html = htmlRenderer.render(markdownBody);
      newPost.setTitle(yamlMarkdownHeader.getTitle());
      newPost.setAuthor(yamlMarkdownHeader.getAuthor());
      newPost.setPublishTime(yamlMarkdownHeader.getDate());
      newPost.setCatalogue(catalogue);
      newPost.setCatalogueBody(catalogueBody);
      newPost.setCategories(yamlMarkdownHeader.getCategories().stream().map(Category::new).collect(
          Collectors.toList()));
      newPost.setTags(
          yamlMarkdownHeader.getTags().stream().map(Tag::new).collect(Collectors.toList()));
      newPost.setContent(markdownBody.getChildChars().toStringOrNull());
      newPost.setContentBody(html);
      postService.createPost(newPost);
      LOG.info("Post: [{}] save to database success", file.getName());
    }
  }
}
