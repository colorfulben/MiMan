

namespace DAL
{
    using System;
    using System.Collections.Generic;
    using System.Configuration;
    using System.IO;
    using System.Linq;
    using System.Text;
    using System.Threading.Tasks;
    using System.Diagnostics.Contracts;

    [ContractClass(typeof(IComicRepositoryContract))]
    public interface IImageRepository
    {
        string BookTitle { get; set; }
        Task Create(string name, Stream stream);
    }

    [ContractClassFor(typeof(IImageRepository))]
    sealed class IComicRepositoryContract : IImageRepository
    {
        public string BookTitle
        {
            get
            {
                return null;
            }
            set
            {
                Contract.Requires(!string.IsNullOrWhiteSpace(value), "Book title must not be empty.");
                Contract.Requires(string.IsNullOrWhiteSpace(this.BookTitle), "Book title has already been set.");
            }
        }

        public Task Create(string name, Stream stream)
        {
            Contract.Requires<InvalidOperationException>(!string.IsNullOrWhiteSpace(this.BookTitle), "Book title hasn't been set");
            return new Task(() => { });
        }
    }

}
