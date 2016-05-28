namespace DAL
{
    using System.Threading.Tasks;


    /// <summary>
    /// 
    /// </summary>
    public interface IRepository<T, TId>
    {
        Task<bool> CreateAsync(T newItem);
        T GetAsync(TId id);
    }
}
