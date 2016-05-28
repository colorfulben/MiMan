namespace DAL.Entity
{
    using Newtonsoft.Json;
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;

    /// <summary>
    /// 
    /// </summary>
    public class ComicBook
    {
        [Key]
        public int Id { get; set; }
        public int BookId { get; set; }
        public Int64 Category { get; set; }

        [StringLength(500)]
        public string Title { get; set; }

        [StringLength(50)]
        public string InitialLetter { get; set; }

        [StringLength(100)]
        public string Author { get; set; }

        public string Intro { get; set; }

        public byte[] Cover { get; set; }

        public int Visited { get; set; }

        public float Rating { get; set; }

        public int RatedBy { get; set; }

        [JsonIgnore]
        public string EntryPage { get; set; }

        public List<Chapter> Chapters { get; set; }

        public bool Finished { get; set; }

        public bool IsLatest { get; set; }
    }
}
