package chitchat;

/**
 * @author laurent
 */
public class ChitChat {

   private String author;
   private String text;
   private Integer thread;
   private Long createdAt;

   public ChitChat() {}

   public String getAuthor() {
      return author;
   }
   public void setAuthor(String author) {
      this.author = author;
   }

   public String getText() {
      return text;
   }
   public void setText(String text) {
      this.text = text;
   }

   public Integer getThread() {
      return thread;
   }
   public void setThread(Integer thread) {
      this.thread = thread;
   }

   public Long getCreatedAt() {
      return createdAt;
   }
   public void setCreatedAt(Long createdAt) {
      this.createdAt = createdAt;
   }
}
