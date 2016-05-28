namespace DAL.Entity
{
    using Newtonsoft.Json;
    using System.ComponentModel.DataAnnotations;


    /// <summary>
    /// 
    /// </summary>
    public class Chapter
    {
        [Key]
        public int Id { get; set; }
        public int Index { get; set; }
        public int TotalPageNum { get; set; }
        public bool IsVolume { get; set; }
        [MaxLength(50)]
        public string DisplayName { get; set; }

        /// <summary>
        /// Gets or sets the book. Mark it as required to enable cascade deleting
        /// </summary>
        /// <value>
        /// The book.
        /// </value>
        [Required]
        [JsonIgnore] 
        public ComicBook Book { get; set; }
    }
}
